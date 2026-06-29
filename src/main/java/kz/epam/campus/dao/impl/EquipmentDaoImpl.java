package kz.epam.campus.dao.impl;

import kz.epam.campus.dao.EquipmentDao;
import kz.epam.campus.model.Equipment;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class EquipmentDaoImpl implements EquipmentDao {

    private final DataSource ds;

    public EquipmentDaoImpl(DataSource ds) {
        this.ds = ds;
    }

    public Equipment save(Equipment e) {
        try (Connection c = ds.getConnection()) {

            if (e.getEquipmentId() == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO equipment(name,description,is_active) VALUES (?,?,?)",
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
                        "UPDATE equipment SET name=?, description=?, is_active=? WHERE equipment_id=?"
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
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM equipment WHERE equipment_id=?")) {

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
        try (Connection c = ds.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM equipment")) {

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
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM equipment WHERE is_active=?")) {

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
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM equipment WHERE equipment_id=?")) {

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