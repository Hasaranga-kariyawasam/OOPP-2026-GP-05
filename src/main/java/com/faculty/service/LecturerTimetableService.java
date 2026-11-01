package com.faculty.service;

import com.faculty.dao.LecturerTimetableDAO;
import com.faculty.dao.LecturerTimetableDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.LecturerTimetableEntry;

import java.sql.SQLException;
import java.util.List;

/** Timetable for lecturers: view only, and only the slots of the lecturer's own courses. */
public class LecturerTimetableService {

    private final LecturerTimetableDAO timetableDAO = new LecturerTimetableDAOImpl();

    public List<LecturerTimetableEntry> getMyTimetable() throws SQLException {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can view the timetable.");
        }
        return timetableDAO.findByLecturer(user.getUserId());
    }
}
