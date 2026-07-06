package kz.epam.campus.services.impl;

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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingCommonDao bookingDao;
    private final SlotDao slotDao;
    private final EquipmentDao equipmentDao;
    private final ScheduleService scheduleService;

    public BookingServiceImpl(BookingCommonDao bookingDao, SlotDao slotDao, EquipmentDao equipmentDao, ScheduleService scheduleService) {
        this.bookingDao = bookingDao;
        this.slotDao = slotDao;
        this.equipmentDao = equipmentDao;
        this.scheduleService = scheduleService;
    }

    @Override
    @Transactional
    public Booking createBooking(int userId, int equipmentId, int slotId, LocalDate date) {

        validateEquipmentAvailability(equipmentId);
        validateAdvanceBookingLimit(date);

        if (!scheduleService.isWorkingDay(date)) {
            throw new BookingException("Lab is closed on this date");
        }

        Slot slot = slotDao.findById(slotId)
                .orElseThrow(() -> new BookingException("Slot not available"));

        if (slot.getEquipmentId() != equipmentId || !slot.getDate().equals(date)) {
            throw new BookingException("Slot not available");
        }

        validateSlotAvailability(slotId);

        if (hasOverlappingBookings(userId, slot)) {
            throw new BookingException("You already have a booking at this time");
        }

        if (countActiveBookingsInWeek(userId, date) >= 2) {
            throw new BookingException("You have reached the limit of 2 bookings per week");
        }

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setSlotId(slotId);
        booking.setEquipmentId(equipmentId);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTimeCreated(LocalDateTime.now());

        return bookingDao.save(booking);
    }

    @Override
    @Transactional
    public void cancelBooking(int bookingId, int userId) {

        Booking booking = bookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));

        if (booking.getUserId() != userId) {
            throw new BookingException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Only confirmed bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingDao.save(booking);
    }

    @Override
    public List<Booking> getUserBookings(int userId) {
        return bookingDao.findByUserId(userId);
    }

    @Override
    public Booking getBookingById(int bookingId) {
        return bookingDao.findById(bookingId)
                .orElseThrow(() -> new BookingException("Booking not found"));
    }

    void validateSlotAvailability(int slotId) {
        Optional<Booking> existing = bookingDao.findActiveBookingBySlotId(slotId);
        if (existing.isPresent()) {
            throw new BookingException("Slot already booked");
        }
    }

    boolean hasOverlappingBookings(int userId, Slot candidateSlot) {

        List<Booking> activeBookings = bookingDao.findActiveBookingsByUserId(userId);

        for (Booking booking : activeBookings) {
            Slot existingSlot = slotDao.findById(booking.getSlotId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Booking references missing slot: " + booking.getSlotId()));

            if (rangesOverlap(candidateSlot, existingSlot)) {
                return true;
            }
        }

        return false;
    }

    int countActiveBookingsInWeek(int userId, LocalDate date) {

        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);

        List<Booking> activeBookings = bookingDao.findActiveBookingsByUserId(userId);

        int count = 0;
        for (Booking booking : activeBookings) {
            Slot slot = slotDao.findById(booking.getSlotId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Booking references missing slot: " + booking.getSlotId()));

            if (!slot.getDate().isBefore(weekStart) && !slot.getDate().isAfter(weekEnd)) {
                count++;
            }
        }

        return count;
    }

    void validateAdvanceBookingLimit(LocalDate date) {

        LocalDate currentWeekStart = LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate latestAllowedDate = currentWeekStart.plusDays(13);

        if (date.isAfter(latestAllowedDate)) {
            throw new BookingException("Cannot book more than 2 weeks in advance");
        }
    }

    void validateEquipmentAvailability(int equipmentId) {
        Equipment equipment = equipmentDao.findById(equipmentId)
                .orElseThrow(() -> new BookingException("Sorry, equipment not available"));

        if (!equipment.isActive()) {
            throw new BookingException("Sorry, equipment not available");
        }
    }

    private boolean rangesOverlap(Slot a, Slot b) {
        if (!a.getDate().equals(b.getDate())) {
            return false;
        }
        return a.getTimeStart().isBefore(b.getTimeEnd()) && b.getTimeStart().isBefore(a.getTimeEnd());
    }
}
