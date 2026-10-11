package com.faculty.service.to;

import com.faculty.dao.to.NoticeQueryDAO;
import com.faculty.dao.to.NoticeQueryDAOImpl;
import com.faculty.exception.to.ServiceException;
import com.faculty.model.to.Notice;

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
