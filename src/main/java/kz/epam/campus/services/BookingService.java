package kz.epam.campus.services;

import kz.epam.campus.model.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {

    Booking createBooking(int userId, int equipmentId, int slotId, LocalDate date);

    void cancelBooking(int bookingId, int userId);

    List<Booking> getUserBookings(int userId);

    Booking getBookingById(int bookingId);
}
