package com.faculty.service;

import com.faculty.dao.NoticeQueryDAO;
import com.faculty.dao.NoticeQueryDAOImpl;
import com.faculty.exception.ServiceException;
import com.faculty.model.Notice;

import java.sql.SQLException;
import java.util.List;

/** Notices a Technical Officer may read (FR-64). */
public class NoticeService {

    private final NoticeQueryDAO noticeDAO = new NoticeQueryDAOImpl();

    public List<Notice> getNotices(int depId) throws ServiceException {
        try {
            return noticeDAO.findForTechnicalOfficer(depId);
        } catch (SQLException e) {
            throw ServiceSupport.wrap("load the notices", e);
        }
    }
}
