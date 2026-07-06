package kz.epam.campus.dao.impl;

import kz.epam.campus.dao.ScheduleDao;
import kz.epam.campus.model.Schedule;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public class ScheduleDaoImpl implements ScheduleDao {
    private static final String TIME_END_DATE_WORKING_DAY_VALUES = """
            INSERT INTO schedules
            ("day",time_start,time_end,date,working_day)
            VALUES (?,?,?,?,?)
            """;

    private final DataSource dataSource;

    public ScheduleDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Schedule save(Schedule schedule) {

        try (Connection c = dataSource.getConnection()) {
            if (schedule.getScheduleId() == 0) {
                try (PreparedStatement ps = c.prepareStatement(
                        TIME_END_DATE_WORKING_DAY_VALUES,
                        Statement.RETURN_GENERATED_KEYS
                )) {
                    ps.setString(1, schedule.getDay());
                    ps.setTime(2, Time.valueOf(schedule.getTimeStart()));
                    ps.setTime(3, Time.valueOf(schedule.getTimeEnd()));
                    ps.setDate(4, Date.valueOf(schedule.getDate()));
                    ps.setBoolean(5, schedule.isWorkingDay());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            schedule.setScheduleId(rs.getInt(1));
                        }
                    }
                }
            } else {
                try (PreparedStatement ps = c.prepareStatement(
                        """
                                UPDATE schedules
                                SET "day"=?, time_start=?, time_end=?, date=?, working_day=?
                                WHERE schedule_id=?
                                """
                )) {
                    ps.setString(1, schedule.getDay());
                    ps.setTime(2, Time.valueOf(schedule.getTimeStart()));
                    ps.setTime(3, Time.valueOf(schedule.getTimeEnd()));
                    ps.setDate(4, Date.valueOf(schedule.getDate()));
                    ps.setBoolean(5, schedule.isWorkingDay());
                    ps.setInt(6, schedule.getScheduleId());

                    ps.executeUpdate();
                }
            }

            return schedule;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Schedule> findById(Integer id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM schedules WHERE schedule_id=?")) {

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
    public List<Schedule> findAll() {
        try (Connection c = dataSource.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM schedules")) {

            List<Schedule> list = new ArrayList<>();

            while (rs.next()) {
                list.add(map(rs));
            }

            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Schedule> findByDate(LocalDate date) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM schedules WHERE date=?")) {

            ps.setDate(1, Date.valueOf(date));

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
    public void delete(Integer id) {
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM schedules WHERE schedule_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Schedule map(ResultSet rs) throws SQLException {

        Schedule schedule = new Schedule();

        schedule.setScheduleId(rs.getInt("schedule_id"));
        schedule.setDay(rs.getString("day"));
        schedule.setTimeStart(rs.getTime("time_start").toLocalTime());
        schedule.setTimeEnd(rs.getTime("time_end").toLocalTime());
        schedule.setDate(rs.getDate("date").toLocalDate());
        schedule.setWorkingDay(rs.getBoolean("working_day"));

        return schedule;
    }
}