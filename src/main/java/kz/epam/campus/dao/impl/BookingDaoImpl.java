package kz.epam.campus.dao.impl;

import kz.epam.campus.dao.BookingDao;
import kz.epam.campus.model.Booking;
import kz.epam.campus.model.BookingStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class BookingDaoImpl implements BookingDao {

    private final DataSource ds;

    public BookingDaoImpl(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Booking save(Booking b) {
        try (Connection c = ds.getConnection()) {

            if (b.getBookingId() == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO bookings(user_id,slot_id,equipment_id,status,time_created) VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    ps.setInt(1, b.getUserId());
                    ps.setInt(2, b.getSlotId());
                    ps.setInt(3, b.getEquipmentId());
                    ps.setString(4, b.getStatus().name());
                    ps.setTimestamp(5, Timestamp.valueOf(b.getTimeCreated()));
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) b.setBookingId(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE bookings SET status=? WHERE booking_id=?"
                )) {
                    ps.setString(1, b.getStatus().name());
                    ps.setInt(2, b.getBookingId());
                    ps.executeUpdate();
                }
            }

            return b;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Booking> findById(Integer id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM bookings WHERE booking_id=?")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Booking> findAll() {
        try (Connection c = ds.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM bookings")) {

            List<Booking> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(Integer id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM bookings WHERE booking_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Booking> findByUserId(int userId) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM bookings WHERE user_id=?")) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Booking> findActiveBookingBySlotId(int slotId) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM bookings WHERE slot_id=? AND status=?"
             )) {

            ps.setInt(1, slotId);
            ps.setString(2, BookingStatus.CONFIRMED.name());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Booking> findActiveBookingsByUserId(int userId) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM bookings WHERE user_id=? AND status=?"
             )) {

            ps.setInt(1, userId);
            ps.setString(2, BookingStatus.CONFIRMED.name());

            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Booking map(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setUserId(rs.getInt("user_id"));
        b.setSlotId(rs.getInt("slot_id"));
        b.setEquipmentId(rs.getInt("equipment_id"));
        b.setStatus(BookingStatus.valueOf(rs.getString("status")));
        b.setTimeCreated(rs.getTimestamp("time_created").toLocalDateTime());
        return b;
    }
}