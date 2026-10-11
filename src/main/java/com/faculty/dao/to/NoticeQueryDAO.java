package com.faculty.dao.to;

import com.faculty.model.to.Notice;

import java.sql.SQLException;
import java.util.List;

/** Read-only notice queries (the Admin owns create / edit / delete). */
public interface NoticeQueryDAO {

    /** Notices meant for ALL or for technical officers, faculty-wide or for this department, newest first. */
    List<Notice> findForTechnicalOfficer(int depId) throws SQLException;
}
