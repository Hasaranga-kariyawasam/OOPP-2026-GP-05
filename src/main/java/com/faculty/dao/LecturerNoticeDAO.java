package com.faculty.dao;

import com.faculty.model.lecture.LecturerNotice;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: notice queries for the lecturer module. */
public interface LecturerNoticeDAO {

    /** Notices for ALL or LECTURER that are faculty-wide or for the given department, newest first. */
    List<LecturerNotice> findForLecturer(int depId) throws SQLException;
}
