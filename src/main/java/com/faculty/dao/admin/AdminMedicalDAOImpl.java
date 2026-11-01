package com.faculty.dao.admin;

import com.faculty.model.admin.AdminMedicalRow;

import java.sql.SQLException;
import java.util.List;

public class AdminMedicalDAOImpl implements AdminMedicalDAO {

    private static final String SEARCH =
        "SELECT m.medical_id, ug.reg_number, CONCAT(u.first_name,' ',u.last_name) AS student_name, "
      + "       m.session_date, m.approval_status, COALESCE(m.reason,'') AS reason "
      + "FROM medical_record m JOIN undergraduate ug ON ug.stu_id = m.stu_id "
      + "JOIN users u ON u.user_id = ug.stu_id "
      + "WHERE ug.reg_number LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? "
      + "ORDER BY m.session_date DESC";

    @Override
    public List<AdminMedicalRow> search(String text) throws SQLException {
        String like = Jdbc.like(text);
        return Jdbc.query(SEARCH, rs -> new AdminMedicalRow(rs.getInt("medical_id"), rs.getString("reg_number"),
                rs.getString("student_name"), Jdbc.date(rs, "session_date"), rs.getString("approval_status"),
                rs.getString("reason")), like, like, like);
    }

    @Override
    public void updateStatus(int medicalId, String status) throws SQLException {
        Jdbc.update("UPDATE medical_record SET approval_status=? WHERE medical_id=?", status, medicalId);
    }
}
