package com.faculty.dao.admin;

import com.faculty.config.DBConnection;
import com.faculty.model.admin.AdminStaff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class AdminStaffDAOImpl implements AdminStaffDAO {

    private static final String SEARCH =
        "SELECT u.user_id, u.username, u.first_name, u.last_name, u.email, s.dep_id, d.department_name, "
      + "       COALESCE(l.designation,'') AS designation "
      + "FROM users u JOIN staff s ON s.staff_id = u.user_id "
      + "JOIN department d ON d.dep_id = s.dep_id "
      + "LEFT JOIN lecturer l ON l.lecture_id = u.user_id "
      + "WHERE u.role = ? AND (u.username LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ?) "
      + "ORDER BY u.first_name";

    @Override
    public List<AdminStaff> search(String role, String text) throws SQLException {
        String like = Jdbc.like(text);
        return Jdbc.query(SEARCH, rs -> new AdminStaff(
                rs.getInt("user_id"), rs.getString("username"), rs.getString("first_name"),
                rs.getString("last_name"), rs.getString("email"), rs.getInt("dep_id"),
                rs.getString("department_name"), rs.getString("designation")),
                role, like, like, like, like);
    }

    @Override
    public void insert(String role, AdminStaff s, String passwordHash) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id = UserRows.insert(con, role, s.username(), passwordHash, s.firstName(), s.lastName(), s.email());
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO staff (staff_id, dep_id) VALUES (?,?)")) {
                    ps.setInt(1, id);
                    ps.setInt(2, s.depId());
                    ps.executeUpdate();
                }
                if ("LECTURER".equals(role)) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO lecturer (lecture_id, designation) VALUES (?,?)")) {
                        ps.setInt(1, id);
                        ps.setString(2, blankToNull(s.designation()));
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO technical_officer (to_id) VALUES (?)")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public void update(String role, AdminStaff s, String passwordHash) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                UserRows.update(con, s.userId(), s.username(), passwordHash, s.firstName(), s.lastName(), s.email());
                try (PreparedStatement ps = con.prepareStatement("UPDATE staff SET dep_id=? WHERE staff_id=?")) {
                    ps.setInt(1, s.depId());
                    ps.setInt(2, s.userId());
                    ps.executeUpdate();
                }
                if ("LECTURER".equals(role)) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE lecturer SET designation=? WHERE lecture_id=?")) {
                        ps.setString(1, blankToNull(s.designation()));
                        ps.setInt(2, s.userId());
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
