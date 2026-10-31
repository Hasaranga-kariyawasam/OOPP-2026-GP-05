package com.faculty.dao.admin;

import com.faculty.config.DBConnection;
import com.faculty.model.admin.AdminStudent;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AdminStudentDAOImpl implements AdminStudentDAO {

    private static final String SEARCH =
        "SELECT u.user_id, ug.reg_number, u.first_name, u.last_name, u.username, u.email, ug.batch, ug.status, "
      + "       ug.intake_date, ug.dep_id, d.department_name "
      + "FROM undergraduate ug JOIN users u ON u.user_id = ug.stu_id "
      + "JOIN department d ON d.dep_id = ug.dep_id "
      + "WHERE ug.reg_number LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? OR u.username LIKE ? "
      + "ORDER BY ug.reg_number";

    @Override
    public List<AdminStudent> search(String text) throws SQLException {
        String like = Jdbc.like(text);
        return Jdbc.query(SEARCH, rs -> new AdminStudent(
                rs.getInt("user_id"), rs.getString("reg_number"), rs.getString("first_name"),
                rs.getString("last_name"), rs.getString("username"), rs.getString("email"),
                rs.getString("batch"), rs.getString("status"), Jdbc.date(rs, "intake_date"),
                rs.getInt("dep_id"), rs.getString("department_name")),
                like, like, like, like);
    }

    @Override
    public void insert(AdminStudent s, String passwordHash) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id = UserRows.insert(con, "UNDERGRADUATE", s.username(), passwordHash,
                        s.firstName(), s.lastName(), s.email());
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO undergraduate (stu_id, reg_number, intake_date, status, batch, dep_id) "
                      + "VALUES (?,?,?,?,?,?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, s.regNumber());
                    ps.setDate(3, Date.valueOf(s.intakeDate()));
                    ps.setString(4, s.status());
                    ps.setString(5, s.batch());
                    ps.setInt(6, s.depId());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void update(AdminStudent s, String passwordHash) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                UserRows.update(con, s.userId(), s.username(), passwordHash, s.firstName(), s.lastName(), s.email());
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE undergraduate SET reg_number=?, intake_date=?, status=?, batch=?, dep_id=? "
                      + "WHERE stu_id=?")) {
                    ps.setString(1, s.regNumber());
                    ps.setDate(2, Date.valueOf(s.intakeDate()));
                    ps.setString(3, s.status());
                    ps.setString(4, s.batch());
                    ps.setInt(5, s.depId());
                    ps.setInt(6, s.userId());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
