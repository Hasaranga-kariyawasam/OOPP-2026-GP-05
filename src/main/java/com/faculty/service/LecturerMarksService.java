package com.faculty.service;

import com.faculty.dao.LecturerMarksDAO;
import com.faculty.dao.LecturerMarksDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.CourseStudent;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.MarkEntry;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Business rules for marks (FR-18, FR-19): a lecturer can only work with marks of the
 * courses assigned to him, and every mark must be a number between 0 and 100.
 */
public class LecturerMarksService {

    private final LecturerMarksDAO marksDAO = new LecturerMarksDAOImpl();
    private final LecturerCourseService courseService = new LecturerCourseService();
    private final LecturerGradeService gradeService = new LecturerGradeService();

    private Lecturer requireLecturer() {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can manage marks.");
        }
        return (Lecturer) user;
    }

    public List<CourseStudent> getStudents(int courseId) throws SQLException {
        requireLecturer();
        courseService.requireMyCourse(courseId);
        return marksDAO.findEnrolledStudents(courseId);
    }

    public List<MarkEntry> getMarks(int courseId) throws SQLException {
        requireLecturer();
        courseService.requireMyCourse(courseId);
        return marksDAO.findByCourse(courseId);
    }

    /**
     * Adds a mark, or updates it when this student already has a mark for the same evaluation type.
     * Returns true when a new mark was added, false when an existing one was updated.
     */
    public boolean saveMark(int stuId, int courseId, String evaluationType, String category, String markText)
            throws SQLException {
        Lecturer lecturer = requireLecturer();
        courseService.requireMyCourse(courseId);

        String type = evaluationType == null ? "" : evaluationType.trim().toUpperCase();
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Please enter the evaluation type (e.g. QUIZ1, MID, FINAL).");
        }
        if (type.length() > 30) {
            throw new IllegalArgumentException("Evaluation type is too long (30 characters max).");
        }
        if (!"CA".equals(category) && !"FINAL".equals(category)) {
            throw new IllegalArgumentException("Please choose CA or FINAL as the category.");
        }
        BigDecimal value = parseMark(markText);

        if (!marksDAO.isEnrolled(stuId, courseId)) {
            throw new IllegalArgumentException("This student is not enrolled in the selected course.");
        }

        int existing = marksDAO.findMarkId(stuId, courseId, type);
        boolean added = existing <= 0;
        if (added) {
            marksDAO.insert(stuId, courseId, type, category, value);
        } else {
            marksDAO.update(existing, lecturer.getUserId(), category, value);
        }
        gradeService.syncCourse(courseId);       // final marks / grades follow the marks (FR-22)
        return added;
    }

    public void deleteMark(int markId, int courseId) throws SQLException {
        Lecturer lecturer = requireLecturer();
        courseService.requireMyCourse(courseId);
        if (marksDAO.delete(markId, lecturer.getUserId()) == 0) {
            throw new IllegalArgumentException("That mark could not be deleted.");
        }
        gradeService.syncCourse(courseId);
    }

    /** Marks are out of 100 (project brief). */
    private BigDecimal parseMark(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Please enter the mark.");
        }
        BigDecimal value;
        try {
            value = new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The mark must be a number.");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("The mark must be between 0 and 100.");
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
