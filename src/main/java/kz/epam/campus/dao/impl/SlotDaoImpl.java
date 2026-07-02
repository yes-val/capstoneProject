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

    private static final String DATE_TIME_START_VALUES = "INSERT INTO slots(equipment_id,date,time_start) VALUES (?,?,?)";
    private static final String WHERE_SLOT_ID = "SELECT * FROM slots WHERE slot_id=?";
    private static final String SELECT_FROM_SLOTS = "SELECT * FROM slots";
    private static final String SLOTS_WHERE_SLOT_ID = "DELETE FROM slots WHERE slot_id=?";
    private static final String SLOTS_WHERE_EQUIPMENT_ID_AND_DATE = "SELECT * FROM slots WHERE equipment_id=? AND date=?";
    private static final String SLOT_ID_AND_B_STATUS = """
            SELECT s.* FROM slots s
            WHERE s.equipment_id = ?
            AND s.date = ?
            AND NOT EXISTS (
                SELECT 1 FROM bookings b
                WHERE b.slot_id = s.slot_id
                AND b.status = ?
            )
            """;
    public static final String AND_DATE_LIMIT_1 = "SELECT 1 FROM slots WHERE equipment_id=? AND date=? LIMIT 1";
    private final DataSource ds;

    public SlotDaoImpl(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Slot save(Slot slot) {

        try (Connection c = ds.getConnection()) {

            if (slot.getSlotId() == 0) {

                try (PreparedStatement ps = c.prepareStatement(
                        DATE_TIME_START_VALUES,
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
             PreparedStatement ps = c.prepareStatement(WHERE_SLOT_ID)) {

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
             ResultSet rs = st.executeQuery(SELECT_FROM_SLOTS)) {

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
             PreparedStatement ps = c.prepareStatement(SLOTS_WHERE_SLOT_ID)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Slot> findByEquipmentIdAndDate(int equipmentId, LocalDate date) {

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(SLOTS_WHERE_EQUIPMENT_ID_AND_DATE)) {

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
                     SLOT_ID_AND_B_STATUS
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
             PreparedStatement ps = c.prepareStatement(AND_DATE_LIMIT_1)) {

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