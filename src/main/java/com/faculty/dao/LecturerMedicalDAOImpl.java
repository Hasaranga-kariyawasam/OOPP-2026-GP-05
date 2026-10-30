package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.MedicalEntry;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LecturerMedicalDAOImpl implements LecturerMedicalDAO {

    private static final String FIND_FOR_LECTURER =
        "SELECT m.medical_id, u.reg_number, CONCAT(us.first_name, ' ', us.last_name) AS full_name, "
      + "       m.session_date, m.reason, m.approval_status, "
      + "       CONCAT(tu.first_name, ' ', tu.last_name) AS decided_by "
      + "FROM medical_record m "
      + "JOIN undergraduate u ON u.stu_id = m.stu_id "
      + "JOIN users us ON us.user_id = u.stu_id "
      + "LEFT JOIN users tu ON tu.user_id = m.approved_by "
      + "WHERE m.stu_id IN (SELECT e.stu_id FROM enrollment e "
      + "                   JOIN course c ON c.course_id = e.course_id WHERE c.lecture_id = ?) "
      + "ORDER BY m.session_date DESC, u.reg_number";

    @Override
    public List<MedicalEntry> findForLecturer(int lecturerId) throws SQLException {
        List<MedicalEntry> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_FOR_LECTURER)) {
            ps.setInt(1, lecturerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MedicalEntry e = new MedicalEntry();
                    e.setMedicalId(rs.getInt("medical_id"));
                    e.setRegNumber(rs.getString("reg_number"));
                    e.setStudentName(rs.getString("full_name"));
                    Date d = rs.getDate("session_date");
                    e.setSessionDate(d == null ? null : d.toLocalDate());
                    e.setReason(rs.getString("reason"));
                    e.setApprovalStatus(rs.getString("approval_status"));
                    e.setDecidedBy(rs.getString("decided_by"));
                    list.add(e);
                }
            }
        }
        return list;
    }
}
