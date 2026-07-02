package kz.epam.campus.dao.impl;

import kz.epam.campus.dao.UserDao;
import kz.epam.campus.model.Role;
import kz.epam.campus.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class UserCommonDaoImpl implements UserDao {

    private final DataSource ds;

    public UserCommonDaoImpl(DataSource ds) {
        this.ds = ds;
    }

    public User save(User u) {
        try (Connection c = ds.getConnection()) {

            if (u.getUserId() == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO users(name,email,password_hash,position,is_active) VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    ps.setString(1, u.getName());
                    ps.setString(2, u.getEmail());
                    ps.setString(3, u.getPasswordHash());
                    ps.setString(4, u.getPosition().name());
                    ps.setBoolean(5, u.isActive());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) u.setUserId(rs.getInt(1));
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE users SET name=?, email=?, password_hash=?, position=?, is_active=? WHERE user_id=?"
                )) {
                    ps.setString(1, u.getName());
                    ps.setString(2, u.getEmail());
                    ps.setString(3, u.getPasswordHash());
                    ps.setString(4, u.getPosition().name());
                    ps.setBoolean(5, u.isActive());
                    ps.setInt(6, u.getUserId());

                    ps.executeUpdate();
                }
            }

            return u;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findById(Integer id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE user_id=?")) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM users WHERE email=?")) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<User> findAll() {
        try (Connection c = ds.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users")) {

            List<User> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs));
            return list;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Integer id) {
        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPosition(Role.valueOf(rs.getString("position")));
        u.setActive(rs.getBoolean("is_active"));
        return u;
    }
}