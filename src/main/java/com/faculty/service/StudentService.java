package com.faculty.service.student;

import com.faculty.dao.student.StudentDAO;
import com.faculty.dao.student.StudentDAOImpl;
import com.faculty.model.student.StudentRecords.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Business rules of the student portal:
 *  - each component (theory / practical) has 15 sessions
 *  - an approved medical counts as attended
 *  - attendance of 80 % or more is required (exactly 80 % is fine)
 *  - GPA is credit weighted, grade points come from UGC Circular 12/2024 (stored in the grade table)
 */
public class StudentService {

    public static final int SESSIONS_PER_COMPONENT = 15;
    public static final double MIN_ATTENDANCE = 80.0;
    public static final double MIN_CA = 40.0;

    public static final String THEORY = "THEORY";
    public static final String PRACTICAL = "PRACTICAL";
    public static final String COMBINED = "COMBINED";

    private final StudentDAO dao;

    public StudentService() {
        this(new StudentDAOImpl());
    }

    public StudentService(StudentDAO dao) {
        this.dao = dao;
    }

    public StudentDAO dao() {
        return dao;
    }

    // --------------------------------------------------------------- attendance

    /** Attendance summary per course for THEORY, PRACTICAL or COMBINED view. */
    public List<AttendanceRow> attendanceSummary(int stuId, String view) throws SQLException {
        List<AttendanceCount> counts = dao.findAttendanceCounts(stuId);
        List<AttendanceRow> rows = new ArrayList<>();

        if (COMBINED.equals(view)) {
            // add theory + practical of the same course together
            Map<Integer, int[]> sums = new LinkedHashMap<>();    // {components, recorded, present, medical}
            Map<Integer, AttendanceCount> first = new LinkedHashMap<>();
            for (AttendanceCount c : counts) {
                int[] s = sums.computeIfAbsent(c.courseId(), k -> new int[4]);
                s[0]++;
                s[1] += c.recorded();
                s[2] += c.present();
                s[3] += c.approvedMedical();
                first.putIfAbsent(c.courseId(), c);
            }
            for (Map.Entry<Integer, int[]> e : sums.entrySet()) {
                AttendanceCount c = first.get(e.getKey());
                int[] s = e.getValue();
                rows.add(build(c.courseId(), c.code(), c.name(),
                        s[0] == 1 ? c.component() : "Theory + Practical",
                        s[0] * SESSIONS_PER_COMPONENT, s[1], s[2], s[3]));
            }
        } else {
            for (AttendanceCount c : counts) {
                if (c.component().equals(view)) {
                    rows.add(build(c.courseId(), c.code(), c.name(), c.component(),
                            SESSIONS_PER_COMPONENT, c.recorded(), c.present(), c.approvedMedical()));
                }
            }
        }
        return rows;
    }

    private AttendanceRow build(int courseId, String code, String name, String component,
                                int total, int recorded, int present, int medical) {
        int attended = present + medical;
        double percent = Math.min(100.0, attended * 100.0 / total);
        int absent = Math.max(0, recorded - present);
        return new AttendanceRow(courseId, code, name, component, total, present, absent,
                medical, percent, percent >= MIN_ATTENDANCE);
    }

    /** Attendance % over every course of the student (theory + practical together). */
    public double overallAttendance(int stuId) throws SQLException {
        int attended = 0;
        int total = 0;
        for (AttendanceRow r : attendanceSummary(stuId, COMBINED)) {
            attended += r.present() + r.approvedMedical();
            total += r.totalSessions();
        }
        return total == 0 ? 0 : Math.min(100.0, attended * 100.0 / total);
    }

    /** Components (course + theory/practical) that are below 80 %. */
    public List<AttendanceRow> attendanceAlerts(int stuId) throws SQLException {
        List<AttendanceRow> alerts = new ArrayList<>();
        for (String view : new String[]{THEORY, PRACTICAL}) {
            for (AttendanceRow r : attendanceSummary(stuId, view)) {
                if (!r.eligible()) {
                    alerts.add(r);
                }
            }
        }
        return alerts;
    }

    // --------------------------------------------------------------------- GPA

    /** Credit weighted GPA of the given grades (0 when there are none). */
    public double gpa(List<GradeRow> grades) {
        double points = 0;
        int credits = 0;
        for (GradeRow g : grades) {
            points += g.gradePoint() * g.credits();
            credits += g.credits();
        }
        return credits == 0 ? 0 : points / credits;
    }

    public int totalCredits(List<GradeRow> grades) {
        int credits = 0;
        for (GradeRow g : grades) {
            credits += g.credits();
        }
        return credits;
    }

    /** SGPA for every semester, oldest first. */
    public List<SemesterGpa> semesterGpas(List<GradeRow> grades) {
        Map<String, List<GradeRow>> bySemester = new LinkedHashMap<>();
        for (GradeRow g : grades) {
            bySemester.computeIfAbsent(g.semester(), k -> new ArrayList<>()).add(g);
        }
        List<SemesterGpa> list = new ArrayList<>();
        for (Map.Entry<String, List<GradeRow>> e : bySemester.entrySet()) {
            list.add(new SemesterGpa(e.getKey(), totalCredits(e.getValue()), gpa(e.getValue())));
        }
        return list;
    }
}
