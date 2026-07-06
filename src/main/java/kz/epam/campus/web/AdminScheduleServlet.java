package kz.epam.campus.web;

import kz.epam.campus.services.ScheduleService;
import kz.epam.campus.services.security.SecurityService;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;

@WebServlet("/admin/schedule/*")
public class AdminScheduleServlet extends HttpServlet {

    private ScheduleService scheduleService;
    private SecurityService securityService;

    @Override
    public void init() throws ServletException {
        var ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        scheduleService = ctx.getBean(ScheduleService.class);
        securityService = ctx.getBean(SecurityService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (!securityService.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String dateParam = req.getParameter("date");
        LocalDate date = (dateParam == null || dateParam.isEmpty())
                ? LocalDate.now()
                : LocalDate.parse(dateParam);

        boolean isWorkingDay = scheduleService.isWorkingDay(date);

        req.setAttribute("selectedDate", date);
        req.setAttribute("isWorkingDay", isWorkingDay);
        req.getRequestDispatcher("/WEB-INF/views/admin-schedule.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (!securityService.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String path = req.getPathInfo();
        LocalDate date = LocalDate.parse(req.getParameter("date"));

        if ("/holiday".equals(path)) {
            scheduleService.setHoliday(date);
        } else {
            scheduleService.setWorkingDay(date);
        }

        resp.sendRedirect("/admin/schedule?date=" + date);
    }
}
