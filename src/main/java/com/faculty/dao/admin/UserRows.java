package com.faculty.dao.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** The users-table part shared by the student and staff DAOs; both run it inside their own transaction. */
final class UserRows {

    private UserRows() { }

    /** Inserts into users and returns the new user_id. */
    static int insert(Connection con, String role, String username, String passwordHash,
                      String first, String last, String email) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, password_hash, role, first_name, last_name, email) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role);
            ps.setString(4, first);
            ps.setString(5, last);
            ps.setString(6, email);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    /** passwordHash null or blank = keep the current password. */
    static void update(Connection con, int userId, String username, String passwordHash,
                       String first, String last, String email) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE users SET username=?, first_name=?, last_name=?, email=? WHERE user_id=?")) {
            ps.setString(1, username);
            ps.setString(2, first);
            ps.setString(3, last);
            ps.setString(4, email);
            ps.setInt(5, userId);
            ps.executeUpdate();
        }
        if (passwordHash != null && !passwordHash.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
                ps.setString(1, passwordHash);
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
        }
    }
}
