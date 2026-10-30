package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.AttendanceSummaryRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LecturerAttendanceDAOImpl implements LecturerAttendanceDAO {

    // One row per (student, session type). A student with no attendance rows still appears (LEFT JOIN).
    // "medical_hours" = hours of ABSENT sessions that are covered by an APPROVED medical.
    private static final String SUMMARY =
        "SELECT u.stu_id, u.reg_number, CONCAT(us.first_name, ' ', us.last_name) AS full_name, "
      + "       a.session_type, "
      + "       COALESCE(SUM(a.session_hours), 0) AS total_hours, "
      + "       COALESCE(SUM(CASE WHEN a.status = 'PRESENT' THEN a.session_hours ELSE 0 END), 0) AS present_hours, "
      + "       COALESCE(SUM(CASE WHEN a.status = 'ABSENT' AND m.approval_status = 'APPROVED' "
      + "                         THEN a.session_hours ELSE 0 END), 0) AS medical_hours "
      + "FROM undergraduate u "
      + "JOIN users us ON us.user_id = u.stu_id "
      + "LEFT JOIN attendance a ON a.stu_id = u.stu_id AND a.course_id = ? "
      + "LEFT JOIN medical_record m ON m.medical_id = a.medical_id "
      + "WHERE u.stu_id IN (SELECT stu_id FROM enrollment WHERE course_id = ?) "
      + "GROUP BY u.stu_id, u.reg_number, full_name, a.session_type "
      + "ORDER BY u.reg_number";

    @Override
    public List<AttendanceSummaryRow> findSummaryByCourse(int courseId) throws SQLException {
        Map<Integer, AttendanceSummaryRow> rows = new LinkedHashMap<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SUMMARY)) {
            ps.setInt(1, courseId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int stuId = rs.getInt("stu_id");
                    AttendanceSummaryRow row = rows.get(stuId);
                    if (row == null) {
                        row = new AttendanceSummaryRow();
                        row.setStuId(stuId);
                        row.setRegNumber(rs.getString("reg_number"));
                        row.setStudentName(rs.getString("full_name"));
                        rows.put(stuId, row);
                    }
                    String type = rs.getString("session_type");     // null when the student has no sessions yet
                    if ("THEORY".equals(type)) {
                        row.setTheoryTotal(rs.getDouble("total_hours"));
                        row.setTheoryPresent(rs.getDouble("present_hours"));
                        row.setTheoryMedical(rs.getDouble("medical_hours"));
                    } else if ("PRACTICAL".equals(type)) {
                        row.setPracticalTotal(rs.getDouble("total_hours"));
                        row.setPracticalPresent(rs.getDouble("present_hours"));
                        row.setPracticalMedical(rs.getDouble("medical_hours"));
                    }
                }
            }
        }
        return new ArrayList<>(rows.values());
    }
}
