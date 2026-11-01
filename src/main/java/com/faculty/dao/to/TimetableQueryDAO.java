package com.faculty.dao;

import com.faculty.model.TimetableEntry;

import java.sql.SQLException;
import java.util.List;

/** Read-only timetable queries (the Admin owns create / edit / delete). */
public interface TimetableQueryDAO {

    /** The weekly timetable of one department ordered by day and start time (FR-69). */
    List<TimetableEntry> findByDepartment(int depId) throws SQLException;
}
