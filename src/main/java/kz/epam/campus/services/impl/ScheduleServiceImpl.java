package kz.epam.campus.services.impl;

import kz.epam.campus.dao.BookingCommonDao;
import kz.epam.campus.dao.ScheduleDao;
import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.dao.UserDao;
import kz.epam.campus.model.Booking;
import kz.epam.campus.model.BookingStatus;
import kz.epam.campus.model.Schedule;
import kz.epam.campus.model.Slot;
import kz.epam.campus.services.LabHours;
import kz.epam.campus.services.NotificationService;
import kz.epam.campus.services.ScheduleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleDao scheduleDao;
    private final SlotDao slotDao;
    private final BookingCommonDao bookingDao;
    private final UserDao userDao;
    private final NotificationService notificationService;

    public ScheduleServiceImpl(ScheduleDao scheduleDao, SlotDao slotDao, BookingCommonDao bookingDao,
                                UserDao userDao, NotificationService notificationService) {
        this.scheduleDao = scheduleDao;
        this.slotDao = slotDao;
        this.bookingDao = bookingDao;
        this.userDao = userDao;
        this.notificationService = notificationService;
    }

    @Override
    public boolean isWorkingDay(LocalDate date) {
        return scheduleDao.findByDate(date)
                .map(Schedule::isWorkingDay)
                .orElse(true); // no schedule row => default open
    }

    @Override
    @Transactional
    public void setWorkingDay(LocalDate date) {
        setWorkingDay(date, LabHours.DEFAULT_START, LabHours.DEFAULT_END);
    }

    @Override
    public void setWorkingDay(LocalDate date, LocalTime start, LocalTime end) {
        Schedule schedule = scheduleDao.findByDate(date).orElseGet(Schedule::new);

        schedule.setDate(date);
        schedule.setDay(dayCode(date));
        schedule.setTimeStart(start);
        schedule.setTimeEnd(end);
        schedule.setWorkingDay(true);

        scheduleDao.save(schedule);
    }

    @Override
    @Transactional
    public void setHoliday(LocalDate date) {
        Schedule schedule = scheduleDao.findByDate(date).orElseGet(Schedule::new);

        schedule.setDate(date);
        schedule.setDay(dayCode(date));
        schedule.setWorkingDay(false);

        if (schedule.getScheduleId() == 0) {
            // Unused placeholders: ScheduleDaoImpl.save() does not permit null hours.
            // Nothing should read these values on a non-working day;
            // callers are expected to check isWorkingDay() before accessing timeStart/timeEnd.
            schedule.setTimeStart(LocalTime.MIDNIGHT);
            schedule.setTimeEnd(LocalTime.MIDNIGHT);
        }

        scheduleDao.save(schedule);

        cancelAllBookingsForDate(date);
    }

    private void cancelAllBookingsForDate(LocalDate date) {

        List<Slot> allSlots = slotDao.findAll();

        for (Slot slot : allSlots) {
            if (!slot.getDate().equals(date)) {
                continue;
            }

            Optional<Booking> activeBooking = bookingDao.findActiveBookingBySlotId(slot.getSlotId());
            if (activeBooking.isPresent()) {
                Booking booking = activeBooking.get();
                booking.setStatus(BookingStatus.CANCELLED);
                bookingDao.save(booking);

                userDao.findById(booking.getUserId()).ifPresent(user ->
                        notificationService.sendCancellation(
                                booking.getUserId(), booking.getBookingId(), user.getEmail())
                );
            }
        }
    }

    private String dayCode(LocalDate date) {
        return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toUpperCase();
    }
}
