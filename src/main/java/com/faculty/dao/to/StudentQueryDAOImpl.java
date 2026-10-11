package com.faculty.dao.to;

import com.faculty.config.DBConnection;
import com.faculty.model.student.Undergraduate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentQueryDAOImpl implements StudentQueryDAO {

    @Override
    public List<Undergraduate> findAllActive() throws SQLException {
        String sql = "SELECT ug.stu_id, ug.reg_number, ug.status, ug.batch, ug.dep_id, "
                   + "       u.username, u.first_name, u.last_name "
                   + "FROM undergraduate ug JOIN users u ON u.user_id = ug.stu_id "
                   + "WHERE u.is_active = TRUE ORDER BY ug.reg_number";
        List<Undergraduate> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Undergraduate s = new Undergraduate();
                s.setUserId(rs.getInt("stu_id"));
                s.setUsername(rs.getString("username"));
                s.setFirstName(rs.getString("first_name"));
                s.setLastName(rs.getString("last_name"));
                s.setRegNumber(rs.getString("reg_number"));
                s.setStatus(rs.getString("status"));
                s.setBatch(rs.getString("batch"));
                s.setDepId(rs.getInt("dep_id"));
                list.add(s);
            }
        }
        return list;
    }

    @Override
    public List<String> findBatches() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT DISTINCT batch FROM undergraduate ORDER BY batch");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString(1));
            }
        }
        return list;
    }
}
