import kz.epam.campus.dao.BookingCommonDao;
import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.model.Booking;
import kz.epam.campus.model.BookingStatus;
import kz.epam.campus.model.Equipment;
import kz.epam.campus.model.Slot;
import kz.epam.campus.services.BookingException;
import kz.epam.campus.services.ScheduleService;
import kz.epam.campus.services.impl.BookingServiceImpl;

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
    private BookingServiceImpl bookingService;

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

    @Test
    void shouldCreateBookingWhenValidationPass() {

        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID)).thenReturn(List.of());
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Booking result = bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate);


        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingDao).save(captor.capture());
        assertEquals(USER_ID, captor.getValue().getUserId());
        assertEquals(SLOT_ID, captor.getValue().getSlotId());
        assertEquals(EQUIPMENT_ID, captor.getValue().getEquipmentId());
        assertEquals(BookingStatus.CONFIRMED, captor.getValue().getStatus());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenEquipmentNotFound() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.empty());

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        verify(bookingDao, never()).save(any());
        assertEquals("Sorry, equipment not available", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEquipmentIsInactive() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(false)));

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));
        verify(bookingDao, never()).save(any());
        assertEquals("Sorry, equipment not available", exception.getMessage());
    }

    @Test
    void shouldCreateBookingWhenDateIsAtTwoWeekLimit() {
        LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate latestAllowedDate = currentWeekStart.plusDays(13);

        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(latestAllowedDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, latestAllowedDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID)).thenReturn(List.of());
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, latestAllowedDate);

        verify(bookingDao).save(any(Booking.class));
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenDateExceedsTwoWeekLimit() {

        LocalDate currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate tooFarDate = currentWeekStart.plusDays(14);

        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));


        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, tooFarDate));


        verify(bookingDao, never()).save(any());
        assertEquals("Cannot book more than 2 weeks in advance", exception.getMessage());
    }


    @Test
    void shouldThrowExceptionWhenLabIsClosed() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(false);

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        verify(bookingDao, never()).save(any());
        assertEquals("Lab is closed on this date", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSlotNotFound() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.empty());

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        verify(bookingDao, never()).save(any());
        assertEquals("Slot not available", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSlotDoesNotMatchEquipment() {
        int mismatchedEquipmentId = 999;
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, mismatchedEquipmentId, bookingDate, LocalTime.of(10, 0))));

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        verify(bookingDao, never()).save(any());
        assertEquals("Slot not available", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSlotAlreadyBooked() {
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID))
                .thenReturn(Optional.of(booking(BOOKING_ID, OTHER_USER_ID, SLOT_ID, BookingStatus.CONFIRMED)));

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        verify(bookingDao, never()).save(any());
        assertEquals("Slot already booked", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserHasOverlappingBooking() {
        int existingSlotId = 205;
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID))
                .thenReturn(List.of(booking(2000, USER_ID, existingSlotId, BookingStatus.CONFIRMED)));
        when(slotDao.findById(existingSlotId)).thenReturn(Optional.of(slot(existingSlotId, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        verify(bookingDao, never()).save(any());
        assertEquals("You already have a booking at this time", exception.getMessage());
    }

    @Test
    void shouldCreateBookingWhenExistingBookingSameDayDoesNotOverlap() {
        int existingSlotId = 206;
        when(equipmentDao.findById(EQUIPMENT_ID)).thenReturn(Optional.of(equipment(true)));
        when(scheduleService.isWorkingDay(bookingDate)).thenReturn(true);
        when(slotDao.findById(SLOT_ID)).thenReturn(Optional.of(slot(SLOT_ID, EQUIPMENT_ID, bookingDate, LocalTime.of(10, 0))));
        when(bookingDao.findActiveBookingBySlotId(SLOT_ID)).thenReturn(Optional.empty());
        when(bookingDao.findActiveBookingsByUserId(USER_ID))
                .thenReturn(List.of(booking(2001, USER_ID, existingSlotId, BookingStatus.CONFIRMED)));
        when(slotDao.findById(existingSlotId)).thenReturn(Optional.of(slot(existingSlotId, EQUIPMENT_ID, bookingDate, LocalTime.of(14, 0))));
        when(bookingDao.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate);

        verify(bookingDao).save(any(Booking.class));
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenWeeklyLimitReached() {
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

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate));

        verify(bookingDao, never()).save(any());
        assertEquals("You have reached the limit of 2 bookings per week", exception.getMessage());
    }

    @Test
    void shouldCreateBookingWhenUnderWeeklyLimit() {
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

        Booking result = bookingService.createBooking(USER_ID, EQUIPMENT_ID, SLOT_ID, bookingDate);

        verify(bookingDao).save(any(Booking.class));
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
    }

    @Test
    void shouldCancelBookingWhenOwnedAndConfirmed() {
        Booking existing = booking(BOOKING_ID, USER_ID, SLOT_ID, BookingStatus.CONFIRMED);
        when(bookingDao.findById(BOOKING_ID)).thenReturn(Optional.of(existing));

        bookingService.cancelBooking(BOOKING_ID, USER_ID);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingDao).save(captor.capture());
        assertEquals(BookingStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void shouldThrowExceptionWhenBookingNotFound() {
        when(bookingDao.findById(BOOKING_ID)).thenReturn(Optional.empty());

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, USER_ID));

        verify(bookingDao, never()).save(any());
        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotOwner() {
        Booking existing = booking(BOOKING_ID, USER_ID, SLOT_ID, BookingStatus.CONFIRMED);
        when(bookingDao.findById(BOOKING_ID)).thenReturn(Optional.of(existing));

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, OTHER_USER_ID));

        verify(bookingDao, never()).save(any());
        assertEquals("You can only cancel your own bookings", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenBookingNotConfirmed() {
        Booking existing = booking(BOOKING_ID, USER_ID, SLOT_ID, BookingStatus.CANCELLED);
        when(bookingDao.findById(BOOKING_ID)).thenReturn(Optional.of(existing));

        BookingException exception = assertThrows(BookingException.class,
                () -> bookingService.cancelBooking(BOOKING_ID, USER_ID));

        verify(bookingDao, never()).save(any());
        assertEquals("Only confirmed bookings can be cancelled", exception.getMessage());
    }

    @Test
    void shouldReturnBookingsFromDao() {
        List<Booking> expected = List.of(
                booking(1, USER_ID, SLOT_ID, BookingStatus.CONFIRMED),
                booking(2, USER_ID, SLOT_ID + 1, BookingStatus.CANCELLED));
        when(bookingDao.findByUserId(USER_ID)).thenReturn(expected);

        List<Booking> result = bookingService.getUserBookings(USER_ID);

        verify(bookingDao).findByUserId(USER_ID);
        assertEquals(expected, result);
    }
}
