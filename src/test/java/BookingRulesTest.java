import kz.epam.campus.dao.BookingCommonDao;
import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.NotificationDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.dao.UserDao;
import kz.epam.campus.model.*;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.BookingService;
import kz.epam.campus.services.ScheduleService;
import kz.epam.campus.services.SlotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingRulesTest extends BaseDbTest {

    private BookingService bookingService;
    private SlotService slotService;
    private ScheduleService scheduleService;

    private int userId;
    private int equipmentId;
    private List<Slot> todaySlots;

    @BeforeEach
    void setUp() {
        bookingService = ctx.getBean(BookingService.class);
        slotService = ctx.getBean(SlotService.class);
        scheduleService = ctx.getBean(ScheduleService.class);

        UserDao userDao = ctx.getBean(UserDao.class);
        User user = new User();
        user.setName("Rules Test User");
        user.setEmail("rules-test-" + System.nanoTime() + "@test.com");
        user.setPasswordHash("hash");
        user.setPosition(Role.USER);
        user.setActive(true);
        userId = userDao.save(user).getUserId();

        EquipmentDao equipmentDao = ctx.getBean(EquipmentDao.class);
        Equipment equipment = new Equipment();
        equipment.setName("Rules Test Equipment " + System.nanoTime());
        equipment.setDescription("Test");
        equipment.setActive(true);
        equipmentId = equipmentDao.save(equipment).getEquipmentId();

        slotService.generateSlotsForDate(LocalDate.now());

        SlotDao slotDao = ctx.getBean(SlotDao.class);
        todaySlots = slotDao.findByEquipmentIdAndDate(equipmentId, LocalDate.now());
    }

    @AfterEach
    void tearDown() {
        scheduleService.setWorkingDay(LocalDate.now());
    }

    @Test
    void shouldRejectInactiveEquipment() {

        EquipmentDao equipmentDao = ctx.getBean(EquipmentDao.class);
        Equipment equipment = equipmentDao.findById(equipmentId).orElseThrow();
        equipment.setActive(false);
        equipmentDao.save(equipment);

        Slot slot = todaySlots.get(0);

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.createBooking(userId, equipmentId, slot.getSlotId(), LocalDate.now())
        );

        assertEquals("Sorry, equipment not available", exception.getMessage());
    }

    @Test
    void shouldRejectBookingTooFarInAdvance() {

        LocalDate currentWeekStart = LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        LocalDate tooFarDate = currentWeekStart.plusDays(14);

        slotService.generateSlotsForDate(tooFarDate);
        SlotDao slotDao = ctx.getBean(SlotDao.class);
        List<Slot> futureSlots = slotDao.findByEquipmentIdAndDate(equipmentId, tooFarDate);
        Slot slot = futureSlots.get(0);

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.createBooking(userId, equipmentId, slot.getSlotId(), tooFarDate)
        );

        assertEquals("Cannot book more than 2 weeks in advance", exception.getMessage());
    }

    @Test
    void shouldRejectBookingOnHoliday() {

        scheduleService.setHoliday(LocalDate.now());

        Slot slot = todaySlots.get(0);

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.createBooking(userId, equipmentId, slot.getSlotId(), LocalDate.now())
        );

        assertEquals("Lab is closed on this date", exception.getMessage());
    }

    @Test
    void shouldRejectSlotEquipmentMismatch() {

        EquipmentDao equipmentDao = ctx.getBean(EquipmentDao.class);
        Equipment otherEquipment = new Equipment();
        otherEquipment.setName("Other Equipment " + System.nanoTime());
        otherEquipment.setDescription("Test");
        otherEquipment.setActive(true);
        int otherEquipmentId = equipmentDao.save(otherEquipment).getEquipmentId();

        slotService.generateSlotsForDate(LocalDate.now());
        SlotDao slotDao = ctx.getBean(SlotDao.class);
        List<Slot> otherSlots = slotDao.findByEquipmentIdAndDate(otherEquipmentId, LocalDate.now());
        Slot mismatchedSlot = otherSlots.get(0);

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.createBooking(userId, equipmentId, mismatchedSlot.getSlotId(), LocalDate.now())
        );

        assertEquals("Slot not available", exception.getMessage());
    }

    @Test
    void shouldRejectAlreadyBookedSlot() {

        Slot slot = todaySlots.get(0);

        bookingService.createBooking(userId, equipmentId, slot.getSlotId(), LocalDate.now());

        UserDao userDao = ctx.getBean(UserDao.class);
        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("rules-test-2-" + System.nanoTime() + "@test.com");
        secondUser.setPasswordHash("hash");
        secondUser.setPosition(Role.USER);
        secondUser.setActive(true);
        int secondUserId = userDao.save(secondUser).getUserId();

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.createBooking(secondUserId, equipmentId, slot.getSlotId(), LocalDate.now())
        );

        assertEquals("Slot already booked", exception.getMessage());
    }

    @Test
    void shouldRejectOverlappingTimeForSameUser() {

        Slot firstSlot = todaySlots.get(0);
        bookingService.createBooking(userId, equipmentId, firstSlot.getSlotId(), LocalDate.now());

        EquipmentDao equipmentDao = ctx.getBean(EquipmentDao.class);
        Equipment secondEquipment = new Equipment();
        secondEquipment.setName("Second Equipment " + System.nanoTime());
        secondEquipment.setDescription("Test");
        secondEquipment.setActive(true);
        int secondEquipmentId = equipmentDao.save(secondEquipment).getEquipmentId();

        slotService.generateSlotsForDate(LocalDate.now());
        SlotDao slotDao = ctx.getBean(SlotDao.class);
        List<Slot> secondEquipmentSlots = slotDao.findByEquipmentIdAndDate(secondEquipmentId, LocalDate.now());

        Slot sameTimeSlotDifferentEquipment = secondEquipmentSlots.stream()
                .filter(s -> s.getTimeStart().equals(firstSlot.getTimeStart()))
                .findFirst()
                .orElseThrow();

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.createBooking(userId, secondEquipmentId, sameTimeSlotDifferentEquipment.getSlotId(), LocalDate.now())
        );

        assertEquals("You already have a booking at this time", exception.getMessage());
    }

    @Test
    void shouldRejectThirdBookingInSameWeek() {

        bookingService.createBooking(userId, equipmentId, todaySlots.get(0).getSlotId(), LocalDate.now());
        bookingService.createBooking(userId, equipmentId, todaySlots.get(1).getSlotId(), LocalDate.now());

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.createBooking(userId, equipmentId, todaySlots.get(2).getSlotId(), LocalDate.now())
        );

        assertEquals("You have reached the limit of 2 bookings per week", exception.getMessage());
    }

    @Test
    void shouldRejectCancelByWrongUser() {

        Booking booking = bookingService.createBooking(userId, equipmentId, todaySlots.get(0).getSlotId(), LocalDate.now());

        UserDao userDao = ctx.getBean(UserDao.class);
        User otherUser = new User();
        otherUser.setName("Other User");
        otherUser.setEmail("rules-test-other-" + System.nanoTime() + "@test.com");
        otherUser.setPasswordHash("hash");
        otherUser.setPosition(Role.USER);
        otherUser.setActive(true);
        int otherUserId = userDao.save(otherUser).getUserId();

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.cancelBooking(booking.getBookingId(), otherUserId)
        );

        assertEquals("You can only cancel your own bookings", exception.getMessage());
    }

    @Test
    void shouldRejectCancelWhenAlreadyCancelled() {

        Booking booking = bookingService.createBooking(userId, equipmentId, todaySlots.get(0).getSlotId(), LocalDate.now());
        bookingService.cancelBooking(booking.getBookingId(), userId);

        BookingException exception = assertThrows(BookingException.class, () ->
                bookingService.cancelBooking(booking.getBookingId(), userId)
        );

        assertEquals("Only confirmed bookings can be cancelled", exception.getMessage());
    }

    @Test
    void shouldCancelExistingBookingsForThatDateWhenSettingHoliday() {

        Booking booking = bookingService.createBooking(userId, equipmentId, todaySlots.get(0).getSlotId(), LocalDate.now());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());

        scheduleService.setHoliday(LocalDate.now());

        BookingCommonDao bookingDao = ctx.getBean(BookingCommonDao.class);
        Booking afterHoliday = bookingDao.findById(booking.getBookingId()).orElseThrow();

        assertEquals(BookingStatus.CANCELLED, afterHoliday.getStatus());

        NotificationDao notificationDao = ctx.getBean(NotificationDao.class);
        List<Notification> notifications = notificationDao.findByUserId(userId);

        boolean hasCancellationNotification = notifications.stream()
                .anyMatch(n -> n.getBookingId() == booking.getBookingId()
                        && n.getStatus() == NotificationStatus.CANCELLATION);

        assertTrue(hasCancellationNotification, "Expected a CANCELLATION notification for this booking");
    }
}
