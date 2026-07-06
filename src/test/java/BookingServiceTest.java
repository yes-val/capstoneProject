import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.dao.UserDao;
import kz.epam.campus.services.BookingService;
import kz.epam.campus.model.Booking;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Role;
import kz.epam.campus.model.Slot;
import kz.epam.campus.model.User;
import kz.epam.campus.services.SlotService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest extends BaseDbTest {

    private final BookingService bookingService =
            ctx.getBean(BookingService.class);

    private static int userId;
    private static int equipmentId;
    private static int slotId1;
    private static int slotId2;

    @BeforeAll
    static void seedData() {
        UserDao userDao = ctx.getBean(UserDao.class);
        User user = new User();
        user.setName("Test User");
        user.setEmail("booking-test@test.com");
        user.setPasswordHash("hash");
        user.setPosition(Role.USER);
        user.setActive(true);
        userId = userDao.save(user).getUserId();

        EquipmentDao equipmentDao = ctx.getBean(EquipmentDao.class);
        Equipment equipment = new Equipment();
        equipment.setName("Test Equipment");
        equipment.setDescription("Test");
        equipment.setActive(true);
        equipmentId = equipmentDao.save(equipment).getEquipmentId();

        ctx.getBean(SlotService.class).generateSlotsForDate(LocalDate.now());

        SlotDao slotDao = ctx.getBean(SlotDao.class);
        List<Slot> slots = slotDao.findByEquipmentIdAndDate(equipmentId, LocalDate.now());
        slotId1 = slots.get(0).getSlotId();
        slotId2 = slots.get(1).getSlotId();
    }

    @Test
    void shouldCreateBooking() {
        Booking b = bookingService.createBooking(userId, equipmentId, slotId1, LocalDate.now());

        assertTrue(b.getBookingId() > 0);
    }

    @Test
    void shouldCancelBooking() {
        Booking b = bookingService.createBooking(userId, equipmentId, slotId2, LocalDate.now());

        assertDoesNotThrow(() ->
                bookingService.cancelBooking(b.getBookingId(), userId)
        );
    }
}