package com.faculty.dao.admin;

import com.faculty.config.DBConnection;
import com.faculty.util.PasswordUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/** Admin-side account management: register / update / delete people and edit the admin's own profile. */
public final class AdminDAO {

    private AdminDAO() { }

    // ------------------------------------------------------------------ students

    public static void registerStudent(String reg, String first, String last, String username, String email,
                                       String password, String batch, String status,
                                       LocalDate intake, int depId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id = insertUser(con, "UNDERGRADUATE", username, password, first, last, email);
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO undergraduate (stu_id, reg_number, intake_date, status, batch, dep_id) "
                      + "VALUES (?,?,?,?,?,?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, reg);
                    ps.setDate(3, Date.valueOf(intake));
                    ps.setString(4, status);
                    ps.setString(5, batch);
                    ps.setInt(6, depId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** password may be blank = keep the current one. */
    public static void updateStudent(int userId, String reg, String first, String last, String username,
                                     String email, String password, String batch, String status,
                                     LocalDate intake, int depId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                updateUserRow(con, userId, username, password, first, last, email);
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE undergraduate SET reg_number=?, intake_date=?, status=?, batch=?, dep_id=? "
                      + "WHERE stu_id=?")) {
                    ps.setString(1, reg);
                    ps.setDate(2, Date.valueOf(intake));
                    ps.setString(3, status);
                    ps.setString(4, batch);
                    ps.setInt(5, depId);
                    ps.setInt(6, userId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // --------------------------------------------------------------------- staff

    /** role = LECTURER or TECHNICAL_OFFICER. designation is only used for lecturers. */
    public static void registerStaff(String role, String first, String last, String username, String email,
                                     String password, int depId, String designation) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id = insertUser(con, role, username, password, first, last, email);
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO staff (staff_id, dep_id) VALUES (?,?)")) {
                    ps.setInt(1, id);
                    ps.setInt(2, depId);
                    ps.executeUpdate();
                }
                if ("LECTURER".equals(role)) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO lecturer (lecture_id, designation) VALUES (?,?)")) {
                        ps.setInt(1, id);
                        ps.setString(2, designation == null || designation.isBlank() ? null : designation);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = con.prepareStatement(
                            "INSERT INTO technical_officer (to_id) VALUES (?)")) {
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

    public static void updateStaff(String role, int userId, String first, String last, String username,
                                   String email, String password, int depId, String designation) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                updateUserRow(con, userId, username, password, first, last, email);
                try (PreparedStatement ps = con.prepareStatement("UPDATE staff SET dep_id=? WHERE staff_id=?")) {
                    ps.setInt(1, depId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                if ("LECTURER".equals(role)) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE lecturer SET designation=? WHERE lecture_id=?")) {
                        ps.setString(1, designation == null || designation.isBlank() ? null : designation);
                        ps.setInt(2, userId);
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

    /** Deletes the users row; subtype rows follow through ON DELETE CASCADE. */
    public static void deleteUser(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE user_id=?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------- profile

    public static void updateProfile(int userId, String first, String last, String email, String phone,
                                     String nic, LocalDate dob, String street, String city, String postal,
                                     String country, String picturePath) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE users SET first_name=?, last_name=?, email=?, phone=?, nic=?, dob=?, street=?, "
                   + "city=?, postal_code=?, country=?, profile_picture_path=? WHERE user_id=?")) {
            ps.setString(1, first);
            ps.setString(2, last);
            ps.setString(3, email);
            ps.setString(4, nullIfBlank(phone));
            ps.setString(5, nullIfBlank(nic));
            ps.setDate(6, dob == null ? null : Date.valueOf(dob));
            ps.setString(7, nullIfBlank(street));
            ps.setString(8, nullIfBlank(city));
            ps.setString(9, nullIfBlank(postal));
            ps.setString(10, nullIfBlank(country));
            ps.setString(11, nullIfBlank(picturePath));
            ps.setInt(12, userId);
            ps.executeUpdate();
        }
    }

    /** Returns false when the current password is wrong. */
    public static boolean changePassword(int userId, String currentPlain, String newPlain) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            String stored = null;
            try (PreparedStatement ps = con.prepareStatement("SELECT password_hash FROM users WHERE user_id=?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stored = rs.getString(1);
                    }
                }
            }
            if (!PasswordUtil.verify(currentPlain, stored)) {
                return false;
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
                ps.setString(1, PasswordUtil.hash(newPlain));
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
            return true;
        }
    }

    // ------------------------------------------------------------------- helpers

    private static int insertUser(Connection con, String role, String username, String password,
                                  String first, String last, String email) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, password_hash, role, first_name, last_name, email) VALUES (?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));
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

    private static void updateUserRow(Connection con, int userId, String username, String password,
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
        if (password != null && !password.isBlank()) {
            try (PreparedStatement ps = con.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")) {
                ps.setString(1, PasswordUtil.hash(password));
                ps.setInt(2, userId);
                ps.executeUpdate();
            }
        }
    }

    private static String nullIfBlank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
