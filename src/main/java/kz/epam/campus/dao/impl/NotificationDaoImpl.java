package kz.epam.campus.dao.impl;

import kz.epam.campus.dao.NotificationDao;
import kz.epam.campus.model.Notification;
import kz.epam.campus.model.NotificationStatus;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class NotificationDaoImpl implements NotificationDao {

    private final DataSource ds;

    public NotificationDaoImpl(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public List<Notification> findByUserId(int userId) {

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM notifications WHERE user_id=?")) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> list = new ArrayList<>();
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
    public Notification save(Notification notification) {

        try (Connection c = ds.getConnection()) {

            if (notification.getNotificationId() == 0) {

                try (PreparedStatement ps = c.prepareStatement(
                        """
                                INSERT INTO notifications
                                (user_id,booking_id,status,time_sent)
                                VALUES (?,?,?,?)
                                """,
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    ps.setInt(1, notification.getUserId());
                    ps.setInt(2, notification.getBookingId());
                    ps.setString(3, notification.getStatus().name());
                    ps.setTimestamp(4, Timestamp.valueOf(notification.getTimeSent()));

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            notification.setNotificationId(rs.getInt(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE notifications SET status=?, time_sent=? WHERE notification_id=?"
                )) {
                    ps.setString(1, notification.getStatus().name());
                    ps.setTimestamp(2, Timestamp.valueOf(notification.getTimeSent()));
                    ps.setInt(3, notification.getNotificationId());

                    ps.executeUpdate();
                }
            }

            return notification;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Notification> findById(Integer id) {

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM notifications WHERE notification_id=?")) {

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
    public List<Notification> findAll() {

        try (Connection c = ds.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM notifications")) {

            List<Notification> list = new ArrayList<>();

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
             PreparedStatement ps = c.prepareStatement("DELETE FROM notifications WHERE notification_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Notification map(ResultSet rs) throws SQLException {

        Notification notification = new Notification();

        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setBookingId(rs.getInt("booking_id"));
        notification.setStatus(NotificationStatus.valueOf(rs.getString("status")));
        notification.setTimeSent(rs.getTimestamp("time_sent").toLocalDateTime());

        return notification;
    }
}
