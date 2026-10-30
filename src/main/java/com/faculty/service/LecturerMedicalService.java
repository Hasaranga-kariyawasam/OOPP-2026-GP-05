package com.faculty.service;

import com.faculty.dao.LecturerMedicalDAO;
import com.faculty.dao.LecturerMedicalDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.MedicalEntry;

import java.sql.SQLException;
import java.util.List;

/** Medicals for lecturers: view only (FR-17), limited to students of the lecturer's own courses. */
public class LecturerMedicalService {

    private final LecturerMedicalDAO medicalDAO = new LecturerMedicalDAOImpl();

    public List<MedicalEntry> getMedicals() throws SQLException {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can view medical records.");
        }
        return medicalDAO.findForLecturer(user.getUserId());
    }
}
