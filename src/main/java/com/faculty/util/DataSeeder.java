package com.faculty.util;

import com.faculty.config.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Inserts starter accounts so every dashboard can be tested:
 *   1 admin, 1 lecturer, 1 technical officer, 3 undergraduates (regular / repeat / batch missed).
 *
 * Safe to run many times: any username that already exists is skipped.
 * All rows are inserted in ONE transaction (all-or-nothing).
 *
 * Optional args for the admin only:  username password
 */
public class DataSeeder {

    public static void main(String[] args) {
        String adminUser = args.length > 0 ? args[0] : "admin";
        String adminPass = args.length > 1 ? args[1] : "Admin@123";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int depId = getOrCreateDepartment(con, "ICT", "Faculty of Technology");

                seedAdmin(con, depId, adminUser, adminPass);
                seedLecturer(con, depId);
                seedTechnicalOfficer(con, depId);
                seedStudents(con, depId);

                con.commit();
                System.out.println("\nDone. Test logins:");
                System.out.println("  " + adminUser + " / " + adminPass + "   (ADMIN)");
                System.out.println("  lecturer1 / Lecturer@123        (LECTURER)");
                System.out.println("  to1       / Officer@123         (TECHNICAL_OFFICER)");
                System.out.println("  student1  / Student@123         (UNDERGRADUATE - REGULAR)");
                System.out.println("  student2  / Student@123         (UNDERGRADUATE - REPEAT)");
                System.out.println("  student3  / Student@123         (UNDERGRADUATE - BATCH_MISSED)");
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Seeding failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // One method per role: users row + subtype rows
    // ------------------------------------------------------------------

    private static void seedAdmin(Connection con, int depId, String username, String password) throws SQLException {
        int id = createUser(con, username, password, "ADMIN", "System", "Administrator", "admin@faculty.local");
        if (id < 0) return;
        insertStaff(con, id, depId);
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO admin (admin_id) VALUES (?)")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static void seedLecturer(Connection con, int depId) throws SQLException {
        int id = createUser(con, "lecturer1", "Lecturer@123", "LECTURER", "Nimal", "Perera", "lecturer1@faculty.local");
        if (id < 0) return;
        insertStaff(con, id, depId);
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO lecturer (lecture_id, designation) VALUES (?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, "Senior Lecturer");
            ps.executeUpdate();
        }
    }

    private static void seedTechnicalOfficer(Connection con, int depId) throws SQLException {
        int id = createUser(con, "to1", "Officer@123", "TECHNICAL_OFFICER", "Kamal", "Silva", "to1@faculty.local");
        if (id < 0) return;
        insertStaff(con, id, depId);
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO technical_officer (to_id) VALUES (?)")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private static void seedStudents(Connection con, int depId) throws SQLException {
        // username, first, last, email, regNumber, intakeDate, status, batch
        insertStudent(con, depId, "student1", "Sanduni", "Fernando", "student1@faculty.local",
                "TG/2024/1001", "2024-03-01", "REGULAR", "2024");
        insertStudent(con, depId, "student2", "Ruwan", "Jayasinghe", "student2@faculty.local",
                "TG/2023/1002", "2023-03-01", "REPEAT", "2023");
        insertStudent(con, depId, "student3", "Tharindu", "Wickrama", "student3@faculty.local",
                "TG/2022/1003", "2022-03-01", "BATCH_MISSED", "2022");
    }

    private static void insertStudent(Connection con, int depId, String username, String first, String last,
                                      String email, String regNumber, String intakeDate,
                                      String status, String batch) throws SQLException {
        int id = createUser(con, username, "Student@123", "UNDERGRADUATE", first, last, email);
        if (id < 0) return;
        String sql = "INSERT INTO undergraduate (stu_id, reg_number, intake_date, status, batch, dep_id) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, regNumber);
            ps.setDate(3, Date.valueOf(intakeDate));
            ps.setString(4, status);
            ps.setString(5, batch);
            ps.setInt(6, depId);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------------
    // Shared helpers
    // ------------------------------------------------------------------

    /**
     * Inserts the base users row (BCrypt-hashed password).
     * @return the new user_id, or -1 if that username already exists (skipped).
     */
    private static int createUser(Connection con, String username, String password, String role,
                                  String first, String last, String email) throws SQLException {
        if (userExists(con, username)) {
            System.out.println("Skipped '" + username + "' (already exists)");
            return -1;
        }
        String sql = "INSERT INTO users (username, password_hash, role, first_name, last_name, email, "
                   + "phone, street, city, postal_code, country) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, PasswordUtil.hash(password));   // never store plain text
            ps.setString(3, role);
            ps.setString(4, first);
            ps.setString(5, last);
            ps.setString(6, email);
            ps.setString(7, "0710000000");
            ps.setString(8, "Faculty of Technology, University of Ruhuna");
            ps.setString(9, "Matara");
            ps.setString(10, "81000");
            ps.setString(11, "Sri Lanka");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                System.out.println("Created " + role + " '" + username + "'");
                return keys.getInt(1);
            }
        }
    }

    private static void insertStaff(Connection con, int userId, int depId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT INTO staff (staff_id, dep_id) VALUES (?, ?)")) {
            ps.setInt(1, userId);
            ps.setInt(2, depId);
            ps.executeUpdate();
        }
    }

    private static boolean userExists(Connection con, String username) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static int getOrCreateDepartment(Connection con, String name, String faculty) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT dep_id FROM department WHERE department_name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO department (department_name, faculty) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, faculty);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }
}
