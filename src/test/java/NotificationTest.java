import kz.epam.campus.dao.BookingCommonDao;
import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.dao.UserDao;
import kz.epam.campus.model.Booking;
import kz.epam.campus.model.BookingStatus;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Role;
import kz.epam.campus.model.Slot;
import kz.epam.campus.model.User;
import kz.epam.campus.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationTest extends BaseDbTest {

    private final NotificationService notificationService =
            ctx.getBean(NotificationService.class);

    private int userId;
    private int bookingId;

    @BeforeEach
    void setUp() {
        UserDao userDao = ctx.getBean(UserDao.class);
        User user = new User();
        user.setName("Notification Test User");
        user.setEmail("notification-test-" + System.nanoTime() + "@test.com");
        user.setPasswordHash("hash");
        user.setPosition(Role.USER);
        user.setActive(true);
        userId = userDao.save(user).getUserId();

        EquipmentDao equipmentDao = ctx.getBean(EquipmentDao.class);
        Equipment equipment = new Equipment();
        equipment.setName("Notification Test Equipment " + System.nanoTime());
        equipment.setDescription("Test");
        equipment.setActive(true);
        int equipmentId = equipmentDao.save(equipment).getEquipmentId();

        SlotDao slotDao = ctx.getBean(SlotDao.class);
        Slot slot = new Slot();
        slot.setEquipmentId(equipmentId);
        slot.setDate(LocalDate.now());
        slot.setTimeStart(LocalTime.of(9, 0));
        int slotId = slotDao.save(slot).getSlotId();

        BookingCommonDao bookingDao = ctx.getBean(BookingCommonDao.class);
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setSlotId(slotId);
        booking.setEquipmentId(equipmentId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTimeCreated(LocalDateTime.now());
        bookingId = bookingDao.save(booking).getBookingId();
    }

    @Test
    void sendConfirmation_success() {
        assertDoesNotThrow(() ->
                notificationService.sendConfirmation(userId, bookingId, "test@example.com")
        );
    }

    @Test
    void sendCancellation_success() {
        assertDoesNotThrow(() ->
                notificationService.sendCancellation(userId, bookingId, "test@example.com")
        );
    }
}
