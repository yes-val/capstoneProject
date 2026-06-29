package kz.epam.campus.dao.impl;

import kz.epam.campus.dao.SlotDao;
import kz.epam.campus.model.BookingStatus;
import kz.epam.campus.model.Slot;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class SlotDaoImpl implements SlotDao {

    private final DataSource ds;

    public SlotDaoImpl(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Slot save(Slot slot) {

        try (Connection c = ds.getConnection()) {

            if (slot.getSlotId() == 0) {

                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO slots(equipment_id,date,time_start) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    ps.setInt(1, slot.getEquipmentId());
                    ps.setDate(2, Date.valueOf(slot.getDate()));
                    ps.setTime(3, Time.valueOf(slot.getTimeStart()));

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            slot.setSlotId(rs.getInt(1));
                        }
                    }
                }
            }
            return slot;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Slot> findById(Integer id) {

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM slots WHERE slot_id=?")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Slot> findAll() {

        try (Connection c = ds.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM slots")) {

            List<Slot> list = new ArrayList<>();

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
             PreparedStatement ps = c.prepareStatement("DELETE FROM slots WHERE slot_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Slot> findByEquipmentIdAndDate(int equipmentId, LocalDate date) {

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM slots WHERE equipment_id=? AND date=?")) {

            ps.setInt(1, equipmentId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                List<Slot> list = new ArrayList<>();
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
    public List<Slot> findUnbookedByEquipmentIdAndDate(int equipmentId, LocalDate date) {

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     """
                             SELECT s.* FROM slots s
                             WHERE s.equipment_id = ?
                             AND s.date = ?
                             AND NOT EXISTS (
                                 SELECT 1 FROM bookings b
                                 WHERE b.slot_id = s.slot_id
                                 AND b.status = ?
                             )
                             """
             )) {

            ps.setInt(1, equipmentId);
            ps.setDate(2, Date.valueOf(date));
            ps.setString(3, BookingStatus.CONFIRMED.name());

            try (ResultSet rs = ps.executeQuery()) {
                List<Slot> list = new ArrayList<>();
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
    public boolean existsByEquipmentIdAndDate(int equipmentId, LocalDate date) {

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT 1 FROM slots WHERE equipment_id=? AND date=? LIMIT 1")) {

            ps.setInt(1, equipmentId);
            ps.setDate(2, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Slot map(ResultSet rs) throws SQLException {

        Slot slot = new Slot();

        slot.setSlotId(rs.getInt("slot_id"));
        slot.setEquipmentId(rs.getInt("equipment_id"));
        slot.setDate(rs.getDate("date").toLocalDate());
        slot.setTimeStart(rs.getTime("time_start").toLocalTime());

        return slot;
    }
}