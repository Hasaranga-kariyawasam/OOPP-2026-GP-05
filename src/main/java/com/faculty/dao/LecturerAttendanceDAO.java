package com.faculty.dao;

import com.faculty.model.lecture.AttendanceSummaryRow;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: attendance queries for the lecturer module (view only, FR-15). */
public interface LecturerAttendanceDAO {

    /** One row per student enrolled in the course, with theory / practical hours summed up. */
    List<AttendanceSummaryRow> findSummaryByCourse(int courseId) throws SQLException;
}
