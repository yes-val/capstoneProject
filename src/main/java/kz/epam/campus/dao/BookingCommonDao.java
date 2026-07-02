package kz.epam.campus.dao;

import kz.epam.campus.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingCommonDao extends CommonDao<Booking, Integer> {

    List<Booking> findByUserId(int userId);

    Optional<Booking> findActiveBookingBySlotId(int slotId);

    List<Booking> findActiveBookingsByUserId(int userId);
}