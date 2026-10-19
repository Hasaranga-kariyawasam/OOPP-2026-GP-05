package com.faculty.service.lecturer;

import com.faculty.dao.lecturer.LecturerDAO;
import com.faculty.dao.lecturer.LecturerDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.Lecturer;
import com.faculty.service.SessionManager;

import java.sql.SQLException;

/**
 * Lecturer business rules. Every method re-checks that the logged-in user really is a
 * lecturer (SEC-02 / SEC-07), so hiding a button in the GUI is not the only protection.
 * Later features (marks, attendance, materials...) get their own services in the same style.
 */
public class LecturerService {

    private final LecturerDAO lecturerDAO = new LecturerDAOImpl();

    /** The logged-in lecturer; throws if nobody is logged in or the user has another role. */
    private Lecturer requireLecturer() {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can do this.");
        }
        return (Lecturer) user;
    }

    public int getMyCourseCount() throws SQLException {
        return lecturerDAO.countCourses(requireLecturer().getUserId());
    }

    public int getMyStudentCount() throws SQLException {
        return lecturerDAO.countStudents(requireLecturer().getUserId());
    }

    public int getMyMaterialCount() throws SQLException {
        return lecturerDAO.countMaterials(requireLecturer().getUserId());
    }
}
