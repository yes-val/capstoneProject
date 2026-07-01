import kz.epam.campus.dao.BookingDao;
import kz.epam.campus.dao.ScheduleDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.dao.UserDao;
import kz.epam.campus.model.Booking;
import kz.epam.campus.model.BookingStatus;
import kz.epam.campus.model.Schedule;
import kz.epam.campus.model.Slot;
import kz.epam.campus.model.User;
import kz.epam.campus.services.LabHours;
import kz.epam.campus.services.NotificationService;
import kz.epam.campus.services.ScheduleService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestingScheduleService {

    @Mock
    private ScheduleDao scheduleDao;

    @Mock
    private SlotDao slotDao;

    @Mock
    private BookingDao bookingDao;

    @Mock
    private UserDao userDao;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ScheduleService scheduleService;

    private LocalDate date;

    @BeforeEach
    void setUp() {
        date = LocalDate.now().plusWeeks(1);
    }

    private Schedule schedule(int scheduleId, LocalDate date, boolean workingDay, LocalTime start, LocalTime end) {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(scheduleId);
        schedule.setDate(date);
        schedule.setWorkingDay(workingDay);
        schedule.setTimeStart(start);
        schedule.setTimeEnd(end);
        return schedule;
    }

    private Slot slot(int slotId, LocalDate date) {
        Slot slot = new Slot();
        slot.setSlotId(slotId);
        slot.setDate(date);
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

    private User user(int userId, String email) {
        User user = new User();
        user.setUserId(userId);
        user.setEmail(email);
        return user;
    }

    // ---------------------------------------------------------------
    // isWorkingDay
    // ---------------------------------------------------------------

    @Test
    void isWorkingDay_returnsTrue_whenScheduleMarkedAsWorkingDay() {
        // GIVEN
        when(scheduleDao.findByDate(date)).thenReturn(Optional.of(
                schedule(1, date, true, LabHours.DEFAULT_START, LabHours.DEFAULT_END)));

        // WHEN
        boolean result = scheduleService.isWorkingDay(date);

        // THEN
        verify(scheduleDao).findByDate(date);
        assertTrue(result);
    }

    @Test
    void isWorkingDay_returnsFalse_whenScheduleMarkedAsHoliday() {
        // GIVEN
        when(scheduleDao.findByDate(date)).thenReturn(Optional.of(
                schedule(1, date, false, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT)));

        // WHEN
        boolean result = scheduleService.isWorkingDay(date);

        // THEN
        verify(scheduleDao).findByDate(date);
        assertFalse(result);
    }

    @Test
    void isWorkingDay_defaultsToTrue_whenNoScheduleExists() {
        // GIVEN
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());

        // WHEN
        boolean result = scheduleService.isWorkingDay(date);

        // THEN
        verify(scheduleDao).findByDate(date);
        assertTrue(result);
    }

    // ---------------------------------------------------------------
    // setWorkingDay
    // ---------------------------------------------------------------

    @Test
    void setWorkingDay_createsScheduleWithDefaultHours_whenNoneExists() {
        // GIVEN
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());

        // WHEN
        scheduleService.setWorkingDay(date);

        // THEN
        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleDao).save(captor.capture());
        assertEquals(date, captor.getValue().getDate());
        assertEquals(LabHours.DEFAULT_START, captor.getValue().getTimeStart());
        assertEquals(LabHours.DEFAULT_END, captor.getValue().getTimeEnd());
        assertTrue(captor.getValue().isWorkingDay());
    }

    @Test
    void setWorkingDay_withExplicitHours_setsGivenStartAndEnd() {
        // GIVEN
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(12, 0);
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());

        // WHEN
        scheduleService.setWorkingDay(date, start, end);

        // THEN
        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleDao).save(captor.capture());
        assertEquals(start, captor.getValue().getTimeStart());
        assertEquals(end, captor.getValue().getTimeEnd());
    }

    // ---------------------------------------------------------------
    // setHoliday
    // ---------------------------------------------------------------

    @Test
    void setHoliday_newSchedule_setsMidnightPlaceholderHours() {
        // GIVEN
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of());

        // WHEN
        scheduleService.setHoliday(date);

        // THEN
        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleDao).save(captor.capture());
        assertFalse(captor.getValue().isWorkingDay());
        assertEquals(LocalTime.MIDNIGHT, captor.getValue().getTimeStart());
        assertEquals(LocalTime.MIDNIGHT, captor.getValue().getTimeEnd());
    }

    @Test
    void setHoliday_existingSchedule_keepsExistingHoursUnchanged() {
        // GIVEN
        LocalTime existingStart = LocalTime.of(9, 0);
        LocalTime existingEnd = LocalTime.of(18, 0);
        when(scheduleDao.findByDate(date)).thenReturn(Optional.of(
                schedule(5, date, true, existingStart, existingEnd)));
        when(slotDao.findAll()).thenReturn(List.of());

        // WHEN
        scheduleService.setHoliday(date);

        // THEN
        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleDao).save(captor.capture());
        assertFalse(captor.getValue().isWorkingDay());
        assertEquals(existingStart, captor.getValue().getTimeStart());
        assertEquals(existingEnd, captor.getValue().getTimeEnd());
    }

    @Test
    void setHoliday_cancelsActiveBookingOnMatchingDate_andNotifiesUser() {
        // GIVEN
        int slotId = 50;
        int userId = 7;
        int bookingId = 500;
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of(slot(slotId, date)));
        when(bookingDao.findActiveBookingBySlotId(slotId))
                .thenReturn(Optional.of(booking(bookingId, userId, slotId, BookingStatus.CONFIRMED)));
        when(userDao.findById(userId)).thenReturn(Optional.of(user(userId, "user@example.com")));

        // WHEN
        scheduleService.setHoliday(date);

        // THEN
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingDao).save(captor.capture());
        verify(notificationService).sendCancellation(userId, bookingId, "user@example.com");
        assertEquals(BookingStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void setHoliday_ignoresSlotsOnDifferentDate() {
        // GIVEN
        int slotId = 51;
        LocalDate otherDate = date.plusDays(1);
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of(slot(slotId, otherDate)));

        // WHEN
        scheduleService.setHoliday(date);

        // THEN
        verify(bookingDao, never()).findActiveBookingBySlotId(anyInt());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void setHoliday_doesNotCancel_whenNoActiveBookingForSlot() {
        // GIVEN
        int slotId = 52;
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of(slot(slotId, date)));
        when(bookingDao.findActiveBookingBySlotId(slotId)).thenReturn(Optional.empty());

        // WHEN
        scheduleService.setHoliday(date);

        // THEN
        verify(bookingDao, never()).save(any());
        verify(notificationService, never()).sendCancellation(anyInt(), anyInt(), anyString());
    }

    @Test
    void setHoliday_cancelsBooking_butSkipsNotification_whenUserNotFound() {
        // GIVEN
        int slotId = 53;
        int userId = 8;
        int bookingId = 501;
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of(slot(slotId, date)));
        when(bookingDao.findActiveBookingBySlotId(slotId))
                .thenReturn(Optional.of(booking(bookingId, userId, slotId, BookingStatus.CONFIRMED)));
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        // WHEN
        scheduleService.setHoliday(date);

        // THEN
        verify(bookingDao).save(any(Booking.class));
        verify(notificationService, never()).sendCancellation(anyInt(), anyInt(), anyString());
    }
}
