package com.faculty.service;

import com.faculty.dao.LecturerAttendanceDAO;
import com.faculty.dao.LecturerAttendanceDAOImpl;
import com.faculty.dao.LecturerMarksDAO;
import com.faculty.dao.LecturerMarksDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.AttendanceSummaryRow;
import com.faculty.model.lecture.EligibilityRow;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.MarkEntry;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exam eligibility (FR-20, FR-21): a student is eligible when the CA average is at least 40%
 * and the attendance is at least 80% (absences covered by an approved medical count as attended).
 */
public class LecturerEligibilityService {

    public static final double MIN_CA_PERCENT = 40.0;
    public static final double MIN_ATTENDANCE_PERCENT = LecturerAttendanceService.REQUIRED_PERCENT;

    private final LecturerMarksDAO marksDAO = new LecturerMarksDAOImpl();
    private final LecturerAttendanceDAO attendanceDAO = new LecturerAttendanceDAOImpl();
    private final LecturerCourseService courseService = new LecturerCourseService();

    public List<EligibilityRow> getEligibility(int courseId) throws SQLException {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can view eligibility.");
        }
        courseService.requireMyCourse(courseId);

        // CA average per student = mean of that student's marks with category CA
        Map<Integer, double[]> ca = new HashMap<>();      // stuId -> {sum, count}
        for (MarkEntry m : marksDAO.findByCourse(courseId)) {
            if ("CA".equals(m.getCategory())) {
                double[] a = ca.computeIfAbsent(m.getStuId(), k -> new double[2]);
                a[0] += m.getMarksValue().doubleValue();
                a[1] += 1;
            }
        }

        List<EligibilityRow> rows = new ArrayList<>();
        for (AttendanceSummaryRow att : attendanceDAO.findSummaryByCourse(courseId)) {
            double[] a = ca.get(att.getStuId());
            Double caAverage = (a == null) ? null : a[0] / a[1];
            double attendance = att.getEffectivePercent();

            boolean caOk = caAverage != null && caAverage >= MIN_CA_PERCENT;
            boolean attendanceOk = att.getTotalHours() > 0 && attendance >= MIN_ATTENDANCE_PERCENT;
            rows.add(new EligibilityRow(att.getRegNumber(), att.getStudentName(),
                    caAverage, attendance, caOk, attendanceOk));
        }
        return rows;
    }
}
