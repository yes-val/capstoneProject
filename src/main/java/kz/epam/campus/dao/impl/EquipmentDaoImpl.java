package kz.epam.campus.dao.impl;

import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.model.Equipment;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class EquipmentDaoImpl implements EquipmentDao {

    private static final String DESCRIPTION_IS_ACTIVE_VALUES = "INSERT INTO equipment(name,description,is_active) VALUES (?,?,?)";
    private static final String DESCRIPTION_IS_ACTIVE_WHERE_EQUIPMENT_ID = "UPDATE equipment SET name=?, description=?, is_active=? WHERE equipment_id=?";
    private static final String EQUIPMENT_WHERE_EQUIPMENT_ID = "SELECT * FROM equipment WHERE equipment_id=?";
    private static final String FROM_EQUIPMENT = "SELECT * FROM equipment";
    private static final String EQUIPMENT_WHERE_IS_ACTIVE = "SELECT * FROM equipment WHERE is_active=?";
    private static final String WHERE_EQUIPMENT_ID = "DELETE FROM equipment WHERE equipment_id=?";
    private final DataSource dataSource;

    public EquipmentDaoImpl(DataSource ds) {
        this.dataSource = ds;
    }

    public Equipment save(Equipment e) {
        try (Connection c = dataSource.getConnection()) {

            if (e.getEquipmentId() == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        DESCRIPTION_IS_ACTIVE_VALUES,
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    ps.setString(1, e.getName());
                    ps.setString(2, e.getDescription());
                    ps.setBoolean(3, e.isActive());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) e.setEquipmentId(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        DESCRIPTION_IS_ACTIVE_WHERE_EQUIPMENT_ID
                )) {
                    ps.setString(1, e.getName());
                    ps.setString(2, e.getDescription());
                    ps.setBoolean(3, e.isActive());
                    ps.setInt(4, e.getEquipmentId());

                    ps.executeUpdate();
                }
            }

            return e;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public Optional<Equipment> findById(Integer id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(EQUIPMENT_WHERE_EQUIPMENT_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Equipment> findAll() {
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(FROM_EQUIPMENT)) {

            List<Equipment> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public List<Equipment> findAllActive() {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(EQUIPMENT_WHERE_IS_ACTIVE)) {

            ps.setBoolean(1, true);

            try (ResultSet rs = ps.executeQuery()) {
                List<Equipment> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(map(rs));
                }
                return list;
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void delete(Integer id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(WHERE_EQUIPMENT_ID)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Equipment map(ResultSet rs) throws SQLException {
        Equipment e = new Equipment();
        e.setEquipmentId(rs.getInt("equipment_id"));
        e.setName(rs.getString("name"));
        e.setDescription(rs.getString("description"));
        e.setActive(rs.getBoolean("is_active"));
        return e;
    }
}