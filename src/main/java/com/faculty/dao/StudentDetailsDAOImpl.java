package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.lecture.StudentDetails;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentDetailsDAOImpl implements StudentDetailsDAO {

    // password_hash is intentionally not selected (SEC-01).
    private static final String FIND_ALL =
        "SELECT u.user_id, ug.reg_number, u.first_name, u.last_name, u.email, u.nic, u.dob, "
      + "       u.phone, u.street, u.city, u.postal_code, u.country, u.is_active, "
      + "       ug.batch, ug.status, ug.intake_date, d.department_name "
      + "FROM undergraduate ug "
      + "JOIN users u      ON u.user_id = ug.stu_id "
      + "JOIN department d ON d.dep_id  = ug.dep_id "
      + "ORDER BY ug.reg_number";

    @Override
    public List<StudentDetails> findAll() throws SQLException {
        List<StudentDetails> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private StudentDetails mapRow(ResultSet rs) throws SQLException {
        StudentDetails s = new StudentDetails();
        s.setUserId(rs.getInt("user_id"));
        s.setRegNumber(rs.getString("reg_number"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setNic(rs.getString("nic"));
        s.setDob(toLocalDate(rs.getDate("dob")));
        s.setPhone(rs.getString("phone"));
        s.setStreet(rs.getString("street"));
        s.setCity(rs.getString("city"));
        s.setPostalCode(rs.getString("postal_code"));
        s.setCountry(rs.getString("country"));
        s.setActive(rs.getBoolean("is_active"));
        s.setBatch(rs.getString("batch"));
        s.setStatus(rs.getString("status"));
        s.setIntakeDate(toLocalDate(rs.getDate("intake_date")));
        s.setDepartmentName(rs.getString("department_name"));
        return s;
    }

    private LocalDate toLocalDate(Date d) {
        return d == null ? null : d.toLocalDate();
    }
}
