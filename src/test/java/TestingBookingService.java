import kz.epam.campus.dao.BookingCommonDao;
import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.model.Booking;
import kz.epam.campus.model.BookingStatus;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Slot;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.BookingService;
import kz.epam.campus.services.ScheduleService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestingBookingService {

    @Mock
    private BookingCommonDao bookingDao;

    @Mock
    private SlotDao slotDao;

    @Mock
    private EquipmentDao equipmentDao;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private BookingService bookingService;

    private static final int USER_ID = 1;
    private static final int OTHER_USER_ID = 2;
    private static final int EQUIPMENT_ID = 10;
    private static final int SLOT_ID = 100;
    private static final int BOOKING_ID = 1000;

    private LocalDate bookingDate;

    @BeforeEach
    void setUp() {
        bookingDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private Equipment equipment(boolean active) {
        Equipment equipment = new Equipment();
        equipment.setEquipmentId(EQUIPMENT_ID);
        equipment.setActive(active);
        return equipment;
    }

    private Slot slot(int slotId, int equipmentId, LocalDate date, LocalTime timeStart) {
        Slot slot = new Slot();
        slot.setSlotId(slotId);
        slot.setEquipmentId(equipmentId);
        slot.setDate(date);
        slot.setTimeStart(timeStart);
        return slot;
    }

    private Booking booking(int bookingId, int userId, int slotId, BookingStatus status) {
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setUserId(userId);
        booking.setSlotId(slotId);
        booking.setStatus(status);
        return booking;
    }

    // ---------------------------------------------------------------
    // createBooking - happy path
    // ---------------------------------------------------------------

    @Test
    void createBooking_success_whenAllValidationsPass() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID)).thenReturn(List.of());
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Booking result = bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate);

        // THEN
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingDao).save(captor.capture());
        assertEquals(USER_ID, captor.getValue().getUserId());
        assertEquals(SLOT_ID, captor.getValue().getSlotId());
        assertEquals(EQUIPMENT_ID, captor.getValue().getEquipmentId());
        assertEquals(BookingStatus.CONFIRMED, captor.getValue().getStatus());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    // ---------------------------------------------------------------
    // Rule 1: equipment availability
    // ---------------------------------------------------------------

    @Test
    void createBooking_throwsException_whenEquipmentNotFound() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Sorry, equipment not available", exception.getMessage());
    }

    @Test
    void createBooking_throwsException_whenEquipmentInactive() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(false)));

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Sorry, equipment not available", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // Rule 2: advance booking limit (2 weeks)
    // ---------------------------------------------------------------

    @Test
    void createBooking_success_whenDateIsExactlyAtTwoWeekLimit() {
        // GIVEN
        LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate latestAllowedDate = currentWeekStart.plusDays(13);

        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(latestAllowedDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, latestAllowedDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID)).thenReturn(List.of());
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Booking result = bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, latestAllowedDate);

        // THEN
        verify(bookingDao).save(any(Booking.class));
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void createBooking_throwsException_whenDateExceedsTwoWeekLimit() {
        // GIVEN
        LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate tooFarDate = currentWeekStart.plusDays(14);

        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, tooFarDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Cannot book more than 2 weeks in advance", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // Rule 3: lab must be open on the requested date
    // ---------------------------------------------------------------

    @Test
    void createBooking_throwsException_whenLabIsClosedOnDate() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(false);

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Lab is closed on this date", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // Rule 4: slot must exist and match the equipment/date
    // ---------------------------------------------------------------

    @Test
    void createBooking_throwsException_whenSlotNotFound() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.empty());

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Slot not available", exception.getMessage());
    }

    @Test
    void createBooking_throwsException_whenSlotDoesNotMatchEquipment() {
        // GIVEN
        int mismatchedEquipmentId = 999;
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, mismatchedEquipmentId, bookingDate, LocalTime.of(10, 0))));

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Slot not available", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // Rule 5: slot must not already be booked
    // ---------------------------------------------------------------

    @Test
    void createBooking_throwsException_whenSlotAlreadyBooked() {
        // GIVEN
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID))
                .thenReturn(Optional.of(booking(BOOKING_ID, OTHER_USER_ID, SLOT_ID, BookingStatus.CONFIRMED)));

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Slot already booked", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // Rule 6: user cannot have overlapping bookings
    // ---------------------------------------------------------------

    @Test
    void createBooking_throwsException_whenUserHasOverlappingBooking() {
        // GIVEN
        int existingSlotId = 205;
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID))
                .thenReturn(List.of(booking(2000, USER_ID, existingSlotId, BookingStatus.CONFIRMED)));
        when(slotDao.findById(existingSlotId)).thenReturn(Optional.of(slot(existingSlotId, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("You already have a booking at this time", exception.getMessage());
    }

    @Test
    void createBooking_success_whenExistingBookingSameDayDoesNotOverlap() {
        // GIVEN
        int existingSlotId = 206;
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID))
                .thenReturn(List.of(booking(2001, USER_ID, existingSlotId, BookingStatus.CONFIRMED)));
        when(slotDao.findById(existingSlotId)).thenReturn(Optional.of(slot(existingSlotId, EQUIPMENT_ID, bookingDate, LocalTime.of(14, 0))));
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Booking result = bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate);

        // THEN
        verify(bookingDao).save(any(Booking.class));
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    // ---------------------------------------------------------------
    // Rule 7: at most 2 active bookings per week
    // ---------------------------------------------------------------

    @Test
    void createBooking_throwsException_whenWeeklyLimitReached() {
        // GIVEN
        int existingSlotId1 = 300;
        int existingSlotId2 = 301;
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID)).thenReturn(List.of(
                booking(2002, USER_ID, existingSlotId1, BookingStatus.CONFIRMED),
                booking(2003, USER_ID, existingSlotId2, BookingStatus.CONFIRMED)));
        when(slotDao.findById(existingSlotId1)).thenReturn(Optional.of(slot(existingSlotId1, EQUIPMENT_ID, bookingDate, LocalTime.of(14, 0))));
        when(slotDao.findById(existingSlotId2)).thenReturn(Optional.of(slot(existingSlotId2, EQUIPMENT_ID, bookingDate.plusDays(1), LocalTime.of(9, 0))));

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("You have reached the limit of 2 bookings per week", exception.getMessage());
    }

    @Test
    void createBooking_success_whenUnderWeeklyLimit() {
        // GIVEN
        int existingSlotId = 302;
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID))
                .thenReturn(List.of(booking(2004, USER_ID, existingSlotId, BookingStatus.CONFIRMED)));
        when(slotDao.findById(existingSlotId))
                .thenReturn(Optional.of(slot(existingSlotId, EQUIPMENT_ID, bookingDate.plusDays(1), LocalTime.of(9, 0))));
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        Booking result = bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate);

        // THEN
        verify(bookingDao).save(any(Booking.class));
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    // ---------------------------------------------------------------
    // cancelBooking
    // ---------------------------------------------------------------

    @Test
    void cancelBooking_success_whenOwnedAndConfirmed() {
        // GIVEN
        Booking existing = booking(BOOKING_ID, USER_ID, SLOT_ID, BookingStatus.CONFIRMED);
        when(bookingDao.findById(BOOKING_ID)).thenReturn(Optional.of(existing));

        // WHEN
        bookingService.cancelBooking(BOOKING_ID, USER_ID);

        // THEN
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingDao).save(captor.capture());
        assertEquals(BookingStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void cancelBooking_throwsException_whenBookingNotFound() {
        // GIVEN
        when(bookingDao.findById(BOOKING_ID)).thenReturn(Optional.empty());

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, USER_ID));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void cancelBooking_throwsException_whenUserIsNotOwner() {
        // GIVEN
        Booking existing = booking(BOOKING_ID, USER_ID, SLOT_ID, BookingStatus.CONFIRMED);
        when(bookingDao.findById(BOOKING_ID)).thenReturn(Optional.of(existing));

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, OTHER_USER_ID));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("You can only cancel your own bookings", exception.getMessage());
    }

    @Test
    void cancelBooking_throwsException_whenBookingNotConfirmed() {
        // GIVEN
        Booking existing = booking(BOOKING_ID, USER_ID, SLOT_ID, BookingStatus.CANCELLED);
        when(bookingDao.findById(BOOKING_ID)).thenReturn(Optional.of(existing));

        // WHEN
        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, USER_ID));

        // THEN
        verify(bookingDao, never()).save(any());
        assertEquals("Only confirmed bookings can be cancelled", exception.getMessage());
    }

    // ---------------------------------------------------------------
    // getUserBookings
    // ---------------------------------------------------------------

    @Test
    void getUserBookings_returnsBookingsFromDao() {
        // GIVEN
        List<Booking> expected = List.of(
                booking(1, USER_ID, SLOT_ID, BookingStatus.CONFIRMED),
                booking(2, USER_ID, SLOT_ID + 1, BookingStatus.CANCELLED));
        when(bookingDao.findByUserId(USER_ID)).thenReturn(expected);

        // WHEN
        List<Booking> result = bookingService.getUserBookings(USER_ID);

        // THEN
        verify(bookingDao).findByUserId(USER_ID);
        assertEquals(expected, result);
    }
}
