package com.faculty.service;

import com.faculty.dao.LecturerGradeDAO;
import com.faculty.dao.LecturerGradeDAOImpl;
import com.faculty.dao.LecturerMarksDAO;
import com.faculty.dao.LecturerMarksDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.CourseStudent;
import com.faculty.model.lecture.GpaSummaryRow;
import com.faculty.model.lecture.GradeLine;
import com.faculty.model.lecture.GradeRow;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.MarkEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Final marks, grades (FR-22) and SGPA / CGPA (FR-23).
 * Grades always come from the marks (final = 40% CA average + 60% final exam), and the grade table is kept
 * in step whenever the lecturer changes a mark, so the admin and student screens see the same grades.
 */
public class LecturerGradeService {

    private final LecturerMarksDAO marksDAO = new LecturerMarksDAOImpl();
    private final LecturerGradeDAO gradeDAO = new LecturerGradeDAOImpl();
    private final LecturerCourseService courseService = new LecturerCourseService();

    private void requireLecturer() {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can view grades.");
        }
    }

    // ---------------------------------------------------------------- grades of one course

    /** One row per enrolled student: CA average, final exam, final mark and grade (null while still pending). */
    public List<GradeRow> getCourseGrades(int courseId) throws SQLException {
        requireLecturer();
        courseService.requireMyCourse(courseId);

        List<CourseStudent> students = marksDAO.findEnrolledStudents(courseId);
        Map<Integer, Double[]> parts = collectParts(marksDAO.findByCourse(courseId));

        List<GradeRow> rows = new ArrayList<>();
        for (CourseStudent s : students) {
            Double[] p = parts.get(s.getStuId());
            Double ca = p == null ? null : p[0];
            Double fin = p == null ? null : p[1];
            if (ca != null && fin != null) {
                double mark = GradeScale.finalMark(ca, fin);
                String grade = GradeScale.gradeFor(mark);
                rows.add(new GradeRow(s.getRegNumber(), s.getFullName(), ca, fin, mark, grade, GradeScale.pointFor(grade)));
            } else {
                rows.add(new GradeRow(s.getRegNumber(), s.getFullName(), ca, fin, null, null, null));
            }
        }
        return rows;
    }

    /**
     * Brings the grade table in line with the marks of this course: students with CA marks and a final exam
     * mark get (or keep) a grade row, everybody else has none. The course must already be checked as the
     * lecturer's own (LecturerMarksService does that).
     */
    void syncCourse(int courseId) throws SQLException {
        String semester = gradeDAO.findCourseSemester(courseId);
        if (semester == null) {
            return;
        }
        Map<Integer, Double[]> parts = collectParts(marksDAO.findByCourse(courseId));
        for (CourseStudent s : marksDAO.findEnrolledStudents(courseId)) {
            Double[] p = parts.get(s.getStuId());
            if (p != null && p[0] != null && p[1] != null) {
                double mark = GradeScale.finalMark(p[0], p[1]);
                String grade = GradeScale.gradeFor(mark);
                gradeDAO.saveGrade(s.getStuId(), courseId, BigDecimal.valueOf(mark), grade,
                        BigDecimal.valueOf(GradeScale.pointFor(grade)), semester);
            } else {
                gradeDAO.deleteGrade(s.getStuId(), courseId);
            }
        }
    }

    /** stuId -> {CA average, final exam average}; an entry is null when that kind of mark does not exist yet. */
    private Map<Integer, Double[]> collectParts(List<MarkEntry> marks) {
        Map<Integer, double[]> ca = new HashMap<>();      // {sum, count}
        Map<Integer, double[]> fin = new HashMap<>();
        for (MarkEntry m : marks) {
            Map<Integer, double[]> target = "FINAL".equals(m.getCategory()) ? fin : ca;
            double[] a = target.computeIfAbsent(m.getStuId(), k -> new double[2]);
            a[0] += m.getMarksValue().doubleValue();
            a[1] += 1;
        }
        Map<Integer, Double[]> result = new HashMap<>();
        for (Integer id : unionKeys(ca, fin)) {
            double[] c = ca.get(id);
            double[] f = fin.get(id);
            result.put(id, new Double[]{c == null ? null : c[0] / c[1], f == null ? null : f[0] / f[1]});
        }
        return result;
    }

    private java.util.Set<Integer> unionKeys(Map<Integer, double[]> a, Map<Integer, double[]> b) {
        java.util.Set<Integer> keys = new java.util.HashSet<>(a.keySet());
        keys.addAll(b.keySet());
        return keys;
    }

    // ---------------------------------------------------------------------- SGPA / CGPA

    /**
     * SGPA (latest semester) and CGPA of every undergraduate that has grades, from the grade table.
     * GPA = sum(grade point x credits) / sum(credits).
     */
    public List<GpaSummaryRow> getGpaSummary() throws SQLException {
        requireLecturer();
        Map<Integer, List<GradeLine>> byStudent = new TreeMap<>();
        for (GradeLine l : gradeDAO.findAllLines()) {
            byStudent.computeIfAbsent(l.getStuId(), k -> new ArrayList<>()).add(l);
        }

        List<GpaSummaryRow> rows = new ArrayList<>();
        for (List<GradeLine> lines : byStudent.values()) {
            GradeLine first = lines.get(0);
            String latest = lines.stream().map(GradeLine::getSemester).max(String::compareTo).orElse("");
            int credits = lines.stream().mapToInt(GradeLine::getCredits).sum();
            double cgpa = gpa(lines);
            double sgpa = gpa(lines.stream().filter(l -> latest.equals(l.getSemester())).toList());
            rows.add(new GpaSummaryRow(first.getStuId(), first.getRegNumber(), first.getStudentName(),
                    first.getStatus(), credits, latest, sgpa, cgpa, lines));
        }
        rows.sort((a, b) -> a.getRegNumber().compareTo(b.getRegNumber()));
        return rows;
    }

    /** SGPA of one semester inside the student's grades (for the detail panel). */
    public double semesterGpa(List<GradeLine> lines, String semester) {
        return gpa(lines.stream().filter(l -> semester.equals(l.getSemester())).toList());
    }

    private double gpa(List<GradeLine> lines) {
        double points = 0;
        int credits = 0;
        for (GradeLine l : lines) {
            points += l.getGradePoint().doubleValue() * l.getCredits();
            credits += l.getCredits();
        }
        if (credits == 0) {
            return 0;
        }
        return BigDecimal.valueOf(points / credits).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
