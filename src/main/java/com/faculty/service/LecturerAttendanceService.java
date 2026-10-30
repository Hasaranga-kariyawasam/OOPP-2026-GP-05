package com.faculty.service;

import com.faculty.dao.LecturerAttendanceDAO;
import com.faculty.dao.LecturerAttendanceDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.AttendanceSummaryRow;
import com.faculty.model.lecture.Lecturer;

import java.sql.SQLException;
import java.util.List;

/** Attendance for lecturers: view only (FR-15). Applies the 80% rule and the medical scenarios (FR-14). */
public class LecturerAttendanceService {

    public static final double REQUIRED_PERCENT = 80.0;

    private final LecturerAttendanceDAO attendanceDAO = new LecturerAttendanceDAOImpl();
    private final LecturerCourseService courseService = new LecturerCourseService();

    public List<AttendanceSummaryRow> getAttendance(int courseId) throws SQLException {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can view attendance.");
        }
        courseService.requireMyCourse(courseId);
        return attendanceDAO.findSummaryByCourse(courseId);
    }

    /** True when the student reaches 80%, counting absences covered by an approved medical as attended. */
    public boolean isEligible(AttendanceSummaryRow row) {
        return row.getTotalHours() > 0 && row.getEffectivePercent() >= REQUIRED_PERCENT;
    }

    /** One of the five scenarios from the SRS. */
    public String getScenario(AttendanceSummaryRow row) {
        if (row.getTotalHours() <= 0) {
            return "No sessions yet";
        }
        double raw = row.getRawPercent();
        double effective = row.getEffectivePercent();

        if (raw >= REQUIRED_PERCENT) {
            return raw > REQUIRED_PERCENT ? "More than 80%" : "Exactly 80%";
        }
        if (!row.hasMedical()) {
            return "Less than 80% (no medical)";
        }
        return effective >= REQUIRED_PERCENT ? "More than 80% with medical" : "Less than 80% with medical";
    }
}
