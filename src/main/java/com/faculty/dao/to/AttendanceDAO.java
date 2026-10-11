package com.faculty.dao.to;

import com.faculty.model.to.Attendance;
import com.faculty.model.to.AttendanceSheetRow;
import com.faculty.model.to.AttendanceSummary;
import com.faculty.model.to.SessionType;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** ABSTRACTION: the service layer talks to this interface, never to JDBC (FR-28 .. FR-32). */
public interface AttendanceDAO {

    /** Enrolled students of the course with their saved status for that session date (PRESENT if nothing saved yet). */
    List<AttendanceSheetRow> findSheet(int courseId, SessionType type, LocalDate date) throws SQLException;

    /** session number -> date for every session already recorded for this course component. */
    Map<Integer, LocalDate> findRecordedSessions(int courseId, SessionType type) throws SQLException;

    /** Saves (insert or update) one whole session sheet in ONE transaction. */
    void saveSheet(int toId, int courseId, SessionType type, int sessionNo, LocalDate date,
                   List<AttendanceSheetRow> rows) throws SQLException;

    /** Removes every record of one session; returns how many rows were deleted. */
    int deleteSession(int courseId, SessionType type, LocalDate date) throws SQLException;

    /**
     * Summary rows (one per student and course). Any filter may be null:
     * courseId, stuId, type (null = theory + practical combined) and batch.
     */
    List<AttendanceSummary> findSummaries(Integer courseId, Integer stuId, SessionType type, String batch)
            throws SQLException;

    /** Every recorded session of one student in one course (theory first, then practical). */
    List<Attendance> findByStudentAndCourse(int stuId, int courseId) throws SQLException;

    /** How many attendance rows this Technical Officer has entered. */
    int countRecordedBy(int toId) throws SQLException;
}
