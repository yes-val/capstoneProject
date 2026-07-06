package kz.epam.campus.dao;

import kz.epam.campus.model.Schedule;

import java.time.LocalDate;
import java.util.Optional;

public interface ScheduleDao extends CommonDao<Schedule, Integer> {

    Optional<Schedule> findByDate(LocalDate date);
}