package kz.epam.campus.services;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ScheduleService {

    boolean isWorkingDay(LocalDate date);

    void setWorkingDay(LocalDate date);

    void setWorkingDay(LocalDate date, LocalTime start, LocalTime end);

    void setHoliday(LocalDate date);
}
