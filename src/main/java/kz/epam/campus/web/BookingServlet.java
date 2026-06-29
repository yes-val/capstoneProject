package kz.epam.campus.web;

import kz.epam.campus.model.Booking;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Slot;
import kz.epam.campus.model.User;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.BookingService;
import kz.epam.campus.services.EquipmentService;
import kz.epam.campus.services.SlotService;
import kz.epam.campus.services.UserService;
import kz.epam.campus.services.security.CurrentUserService;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/bookings/*")
public class BookingServlet extends HttpServlet {

    private BookingService bookingService;
    private EquipmentService equipmentService;
    private SlotService slotService;
    private UserService userService;
    private CurrentUserService currentUserService;

    public static class BookingView {
        public final Booking booking;
        public final Equipment equipment;
        public final Slot slot;

        public BookingView(Booking booking, Equipment equipment, Slot slot) {
            this.booking = booking;
            this.equipment = equipment;
            this.slot = slot;
        }
    }

    @Override
    public void init() throws ServletException {
        var ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        bookingService = ctx.getBean(BookingService.class);
        equipmentService = ctx.getBean(EquipmentService.class);
        slotService = ctx.getBean(SlotService.class);
        userService = ctx.getBean(UserService.class);
        currentUserService = ctx.getBean(CurrentUserService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        User currentUser = getCurrentUser();
        List<Booking> myBookings = bookingService.getUserBookings(currentUser.getUserId());
        List<BookingView> bookingViews = new java.util.ArrayList<>();
        for (Booking b : myBookings) {
            Equipment eq = equipmentService.getById(b.getEquipmentId());
            Slot slot = slotService.getSlotById(b.getSlotId());
            bookingViews.add(new BookingView(b, eq, slot));
        }
        req.setAttribute("myBookings", bookingViews);

        String equipmentIdParam = req.getParameter("equipmentId");

        if (equipmentIdParam == null) {
            List<Equipment> activeEquipment = equipmentService.getActiveEquipment();
            req.setAttribute("equipmentList", activeEquipment);
            req.getRequestDispatcher("/WEB-INF/views/bookings.jsp").forward(req, resp);
            return;
        }

        int equipmentId = Integer.parseInt(equipmentIdParam);
        String dateParam = req.getParameter("date");
        LocalDate date = (dateParam == null || dateParam.isEmpty())
                ? LocalDate.now()
                : LocalDate.parse(dateParam);

        Equipment equipment = equipmentService.getById(equipmentId);
        List<Slot> availableSlots = slotService.getAvailableSlots(equipmentId, date);

        req.setAttribute("equipment", equipment);
        req.setAttribute("selectedDate", date);
        req.setAttribute("availableSlots", availableSlots);
        req.getRequestDispatcher("/WEB-INF/views/slots.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getPathInfo();

        if ("/cancel".equals(path)) {
            handleCancel(req, resp);
        } else {
            handleCreate(req, resp);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        User currentUser = getCurrentUser();

        int equipmentId = Integer.parseInt(req.getParameter("equipmentId"));
        int slotId = Integer.parseInt(req.getParameter("slotId"));
        LocalDate date = LocalDate.parse(req.getParameter("date"));

        try {
            bookingService.createBooking(currentUser.getUserId(), equipmentId, slotId, date);
            resp.sendRedirect("/bookings");
        } catch (BookingException e) {
            req.setAttribute("error", e.getMessage());
            req.setAttribute("equipment", equipmentService.getById(equipmentId));
            req.setAttribute("selectedDate", date);
            req.setAttribute("availableSlots", slotService.getAvailableSlots(equipmentId, date));
            req.getRequestDispatcher("/WEB-INF/views/slots.jsp").forward(req, resp);
        }
    }

    private void handleCancel(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        User currentUser = getCurrentUser();
        int bookingId = Integer.parseInt(req.getParameter("bookingId"));

        try {
            bookingService.cancelBooking(bookingId, currentUser.getUserId());
            resp.sendRedirect("/bookings");
        } catch (BookingException e) {
            String encodedError = java.net.URLEncoder.encode(e.getMessage(), "UTF-8");
            resp.sendRedirect("/bookings?error=" + encodedError);
        }
    }

    private User getCurrentUser() {
        String email = currentUserService.getCurrentUsername()
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));
        return userService.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
