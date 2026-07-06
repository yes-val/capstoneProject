import kz.epam.campus.dao.BookingCommonDao;
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
import kz.epam.campus.services.impl.ScheduleServiceImpl;

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
    private BookingCommonDao bookingDao;

    @Mock
    private UserDao userDao;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

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

    @Test
    void shouldReturnTrueWhenScheduleMarkedAsWorkingDay() {
        when(scheduleDao.findByDate(date)).thenReturn(Optional.of(
                schedule(1, date, true, LabHours.DEFAULT_START, LabHours.DEFAULT_END)));

        boolean result = scheduleService.isWorkingDay(date);

        verify(scheduleDao).findByDate(date);
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenScheduleMarkedAsHoliday() {
        when(scheduleDao.findByDate(date)).thenReturn(Optional.of(
                schedule(1, date, false, LocalTime.MIDNIGHT, LocalTime.MIDNIGHT)));

        boolean result = scheduleService.isWorkingDay(date);

        verify(scheduleDao).findByDate(date);
        assertFalse(result);
    }

    @Test
    void shouldDefaultToTrueWhenNoScheduleExists() {
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());

        boolean result = scheduleService.isWorkingDay(date);

        verify(scheduleDao).findByDate(date);
        assertTrue(result);
    }

    @Test
    void shouldCreateScheduleWithDefaultHoursWhenNoneExists() {
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());

        scheduleService.setWorkingDay(date);

        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleDao).save(captor.capture());
        assertEquals(date, captor.getValue().getDate());
        assertEquals(LabHours.DEFAULT_START, captor.getValue().getTimeStart());
        assertEquals(LabHours.DEFAULT_END, captor.getValue().getTimeEnd());
        assertTrue(captor.getValue().isWorkingDay());
    }

    @Test
    void shouldSetGivenStartAndEndWithExplicitHours() {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(12, 0);
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());

        scheduleService.setWorkingDay(date, start, end);

        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleDao).save(captor.capture());
        assertEquals(start, captor.getValue().getTimeStart());
        assertEquals(end, captor.getValue().getTimeEnd());
    }

    @Test
    void shouldSetMidnightPlaceholderHoursForNewSchedule() {
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of());

        scheduleService.setHoliday(date);

        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleDao).save(captor.capture());
        assertFalse(captor.getValue().isWorkingDay());
        assertEquals(LocalTime.MIDNIGHT, captor.getValue().getTimeStart());
        assertEquals(LocalTime.MIDNIGHT, captor.getValue().getTimeEnd());
    }

    @Test
    void shouldKeepExistingHoursUnchangedForExistingSchedule() {
        LocalTime existingStart = LocalTime.of(9, 0);
        LocalTime existingEnd = LocalTime.of(18, 0);
        when(scheduleDao.findByDate(date)).thenReturn(Optional.of(
                schedule(5, date, true, existingStart, existingEnd)));
        when(slotDao.findAll()).thenReturn(List.of());

        scheduleService.setHoliday(date);

        ArgumentCaptor<Schedule> captor = ArgumentCaptor.forClass(Schedule.class);
        verify(scheduleDao).save(captor.capture());
        assertFalse(captor.getValue().isWorkingDay());
        assertEquals(existingStart, captor.getValue().getTimeStart());
        assertEquals(existingEnd, captor.getValue().getTimeEnd());
    }

    @Test
    void shouldCancelActiveBookingAndNotifyUserOnMatchingDate() {
        int slotId = 50;
        int userId = 7;
        int bookingId = 500;
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of(slot(slotId, date)));
        when(bookingDao.findActiveBookingBySlotId(slotId))
                .thenReturn(Optional.of(booking(bookingId, userId, slotId, BookingStatus.CONFIRMED)));
        when(userDao.findById(userId)).thenReturn(Optional.of(user(userId, "user@example.com")));

        scheduleService.setHoliday(date);

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingDao).save(captor.capture());
        verify(notificationService).sendCancellation(userId, bookingId, "user@example.com");
        assertEquals(BookingStatus.CANCELLED, captor.getValue().getStatus());
    }

    @Test
    void shouldIgnoreSlotsOnDifferentDate() {
        int slotId = 51;
        LocalDate otherDate = date.plusDays(1);
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of(slot(slotId, otherDate)));

        scheduleService.setHoliday(date);

        verify(bookingDao, never()).findActiveBookingBySlotId(anyInt());
        verify(bookingDao, never()).save(any());
    }

    @Test
    void shouldNotCancelWhenNoActiveBookingForSlot() {
        int slotId = 52;
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of(slot(slotId, date)));
        when(bookingDao.findActiveBookingBySlotId(slotId)).thenReturn(Optional.empty());

        scheduleService.setHoliday(date);

        verify(bookingDao, never()).save(any());
        verify(notificationService, never()).sendCancellation(anyInt(), anyInt(), anyString());
    }

    @Test
    void shouldCancelBookingButSkipNotificationWhenUserNotFound() {
        int slotId = 53;
        int userId = 8;
        int bookingId = 501;
        when(scheduleDao.findByDate(date)).thenReturn(Optional.empty());
        when(slotDao.findAll()).thenReturn(List.of(slot(slotId, date)));
        when(bookingDao.findActiveBookingBySlotId(slotId))
                .thenReturn(Optional.of(booking(bookingId, userId, slotId, BookingStatus.CONFIRMED)));
        when(userDao.findById(userId)).thenReturn(Optional.empty());

        scheduleService.setHoliday(date);

        verify(bookingDao).save(any(Booking.class));
        verify(notificationService, never()).sendCancellation(anyInt(), anyInt(), anyString());
    }
}
