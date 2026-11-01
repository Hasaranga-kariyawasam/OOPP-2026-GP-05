package com.faculty.dao.admin;

import com.faculty.model.admin.AdminAttendanceRow;

import java.sql.SQLException;
import java.util.List;

public class AdminAttendanceDAOImpl implements AdminAttendanceDAO {

    private static final String SEARCH =
        "SELECT a.attendance_id, ug.reg_number, c.course_code, a.session_type, a.session_date, a.session_id, a.status "
      + "FROM attendance a JOIN undergraduate ug ON ug.stu_id = a.stu_id "
      + "JOIN course c ON c.course_id = a.course_id "
      + "WHERE ug.reg_number LIKE ? OR c.course_code LIKE ? "
      + "ORDER BY a.session_date DESC LIMIT ?";

    @Override
    public List<AdminAttendanceRow> search(String text, int limit) throws SQLException {
        String like = Jdbc.like(text);
        return Jdbc.query(SEARCH, rs -> new AdminAttendanceRow(rs.getInt("attendance_id"), rs.getString("reg_number"),
                rs.getString("course_code"), rs.getString("session_type"), Jdbc.date(rs, "session_date"),
                rs.getInt("session_id"), rs.getString("status")), like, like, limit);
    }

    @Override
    public void updateStatus(int attendanceId, String status) throws SQLException {
        Jdbc.update("UPDATE attendance SET status=? WHERE attendance_id=?", status, attendanceId);
    }
}
