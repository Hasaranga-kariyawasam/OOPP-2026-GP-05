package com.faculty.service;

import com.faculty.dao.LecturerCourseDAO;
import com.faculty.dao.LecturerCourseDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.LecturerCourse;

import java.sql.SQLException;
import java.util.List;

/** Business rules for "My Courses": a lecturer only ever sees the courses assigned to themselves. */
public class LecturerCourseService {

    private final LecturerCourseDAO courseDAO = new LecturerCourseDAOImpl();

    /**
     * The lecturer id comes from the login session, never from the screen, so one lecturer
     * cannot ask for another lecturer's courses (SEC-02 / SEC-07).
     */
    public List<LecturerCourse> getMyCourses() throws SQLException {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can view assigned courses.");
        }
        return courseDAO.findByLecturer(user.getUserId());
    }

    /** Throws when the course is not one of the logged-in lecturer's own courses. */
    public void requireMyCourse(int courseId) throws SQLException {
        for (LecturerCourse c : getMyCourses()) {
            if (c.getCourseId() == courseId) {
                return;
            }
        }
        throw new SecurityException("This course is not assigned to you.");
    }
}
