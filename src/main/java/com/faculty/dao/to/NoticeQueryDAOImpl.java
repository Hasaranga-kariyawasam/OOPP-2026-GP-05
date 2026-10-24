package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.Notice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoticeQueryDAOImpl implements NoticeQueryDAO {

    @Override
    public List<Notice> findForTechnicalOfficer(int depId) throws SQLException {
        // dep_id NULL = faculty-wide notice (see schema.sql)
        String sql = "SELECT n.notice_id, n.title, n.content, n.posted_date, n.target_role, "
                   + "       a.first_name, a.last_name "
                   + "FROM notice n LEFT JOIN users a ON a.user_id = n.posted_by "
                   + "WHERE n.target_role IN ('ALL', 'TECHNICAL_OFFICER') "
                   + "  AND (n.dep_id IS NULL OR n.dep_id = ?) "
                   + "ORDER BY n.posted_date DESC, n.notice_id DESC";
        List<Notice> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, depId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notice n = new Notice();
                    n.setNoticeId(rs.getInt("notice_id"));
                    n.setTitle(rs.getString("title"));
                    n.setContent(rs.getString("content"));
                    n.setPostedDate(rs.getTimestamp("posted_date").toLocalDateTime());
                    n.setTargetRole(rs.getString("target_role"));
                    String first = rs.getString("first_name");
                    n.setPostedByName(first == null ? "Administration" : first + " " + rs.getString("last_name"));
                    list.add(n);
                }
            }
        }
        return list;
    }
}
