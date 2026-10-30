package com.faculty.dao;

import com.faculty.model.lecture.LecturerTimetableEntry;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: timetable queries for the lecturer module. */
public interface LecturerTimetableDAO {

    /** Slots of the courses taught by this lecturer, ordered by day then start time. */
    List<LecturerTimetableEntry> findByLecturer(int lecturerId) throws SQLException;
}
