package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.LecturerNotice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class LecturerNoticeDAOImpl implements LecturerNoticeDAO {

    // dep_id NULL = faculty-wide notice (see schema.sql)
    private static final String FIND_FOR_LECTURER =
        "SELECT n.notice_id, n.title, n.content, n.posted_date, a.first_name, a.last_name "
      + "FROM notice n LEFT JOIN users a ON a.user_id = n.posted_by "
      + "WHERE n.target_role IN ('ALL', 'LECTURER') AND (n.dep_id IS NULL OR n.dep_id = ?) "
      + "ORDER BY n.posted_date DESC, n.notice_id DESC";

    @Override
    public List<LecturerNotice> findForLecturer(int depId) throws SQLException {
        List<LecturerNotice> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_FOR_LECTURER)) {
            ps.setInt(1, depId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LecturerNotice n = new LecturerNotice();
                    n.setNoticeId(rs.getInt("notice_id"));
                    n.setTitle(rs.getString("title"));
                    n.setContent(rs.getString("content"));
                    Timestamp ts = rs.getTimestamp("posted_date");
                    n.setPostedDate(ts == null ? null : ts.toLocalDateTime());
                    String first = rs.getString("first_name");
                    n.setPostedBy(first == null ? "Administration" : first + " " + rs.getString("last_name"));
                    list.add(n);
                }
            }
        }
        return list;
    }
}
