package com.faculty.service;

import com.faculty.dao.StudentDetailsDAO;
import com.faculty.dao.StudentDetailsDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.StudentDetails;

import java.sql.SQLException;
import java.util.List;

/** Business rules for a lecturer viewing undergraduate details (view only, no editing). */
public class StudentDetailsService {

    private final StudentDetailsDAO dao = new StudentDetailsDAOImpl();

    /** SEC-02 / SEC-07: only a logged-in lecturer may see this data. */
    private void requireLecturer() {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can view undergraduate details.");
        }
    }

    public List<StudentDetails> getAllUndergraduates() throws SQLException {
        requireLecturer();
        return dao.findAll();
    }
}
