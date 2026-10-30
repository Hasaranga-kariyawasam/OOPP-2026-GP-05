package com.faculty.service;

import com.faculty.dao.LecturerNoticeDAO;
import com.faculty.dao.LecturerNoticeDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.Lecturer;
import com.faculty.model.lecture.LecturerNotice;

import java.sql.SQLException;
import java.util.List;

/** Notices for lecturers: view only. */
public class LecturerNoticeService {

    private final LecturerNoticeDAO noticeDAO = new LecturerNoticeDAOImpl();

    public List<LecturerNotice> getMyNotices() throws SQLException {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can view notices.");
        }
        return noticeDAO.findForLecturer(((Lecturer) user).getDepId());
    }
}
