package com.faculty.dao.admin;

import com.faculty.model.admin.AdminNotice;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class AdminNoticeDAOImpl implements AdminNoticeDAO {

    private static final String FIND_ALL =
        "SELECT n.notice_id, n.title, n.target_role, COALESCE(CONCAT(u.first_name,' ',u.last_name),'-') AS posted_by, "
      + "       n.posted_date, n.content "
      + "FROM notice n LEFT JOIN users u ON u.user_id = n.posted_by "
      + "ORDER BY n.posted_date DESC, n.notice_id DESC";

    @Override
    public List<AdminNotice> findAll() throws SQLException {
        return Jdbc.query(FIND_ALL, rs -> {
            Timestamp ts = rs.getTimestamp("posted_date");
            return new AdminNotice(rs.getInt("notice_id"), rs.getString("title"), rs.getString("target_role"),
                    rs.getString("posted_by"), ts == null ? null : ts.toLocalDateTime(), rs.getString("content"));
        });
    }

    @Override
    public void insert(AdminNotice n, Integer postedByAdminId) throws SQLException {
        Jdbc.update("INSERT INTO notice (title, content, target_role, posted_by) VALUES (?,?,?,?)",
                n.title(), n.content(), n.audience(), postedByAdminId);
    }

    @Override
    public void update(AdminNotice n) throws SQLException {
        Jdbc.update("UPDATE notice SET title=?, content=?, target_role=? WHERE notice_id=?",
                n.title(), n.content(), n.audience(), n.noticeId());
    }

    @Override
    public void delete(int noticeId) throws SQLException {
        Jdbc.update("DELETE FROM notice WHERE notice_id=?", noticeId);
    }
}
