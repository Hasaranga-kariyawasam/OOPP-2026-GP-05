package com.faculty.dao.admin;

import com.faculty.model.admin.AdminNotice;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: notice management. */
public interface AdminNoticeDAO {
    List<AdminNotice> findAll() throws SQLException;
    void insert(AdminNotice notice, Integer postedByAdminId) throws SQLException;
    void update(AdminNotice notice) throws SQLException;
    void delete(int noticeId) throws SQLException;
}
