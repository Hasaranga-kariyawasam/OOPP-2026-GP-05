package com.faculty.service.to;

import com.faculty.dao.to.TimetableQueryDAO;
import com.faculty.dao.to.TimetableQueryDAOImpl;
import com.faculty.exception.to.ServiceException;
import com.faculty.model.to.TimetableEntry;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The timetable of the Technical Officer's own department (FR-69). */
public class TimetableService {

    private final TimetableQueryDAO timetableDAO = new TimetableQueryDAOImpl();

    public List<TimetableEntry> getTimetable(int depId) throws ServiceException {
        try {
            return timetableDAO.findByDepartment(depId);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the timetable", e);
        }
    }

    /** Sessions that fall on a given weekday (already sorted by start time). */
    public List<TimetableEntry> filterByDay(List<TimetableEntry> all, DayOfWeek day) {
        String code = dayCode(day);
        List<TimetableEntry> result = new ArrayList<>();
        for (TimetableEntry t : all) {
            if (t.getDay().equals(code)) {
                result.add(t);
            }
        }
        return result;
    }

    /** DayOfWeek.MONDAY -> "MON" (the code stored in the database). */
    public static String dayCode(DayOfWeek day) {
        return day.name().substring(0, 3).toUpperCase(Locale.ROOT);
    }
}
