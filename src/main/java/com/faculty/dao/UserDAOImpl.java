package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.*;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class UserDAOImpl implements UserDAO {

    // One query loads the users row plus whatever subtype data exists (SEC-04: '?' placeholder).
    private static final String FIND_BY_USERNAME =
        "SELECT u.user_id, u.username, u.password_hash, u.role, u.first_name, u.last_name, "
      + "       u.email, u.nic, u.dob, u.phone, u.street, u.city, u.postal_code, u.country, "
      + "       u.profile_picture_path, u.is_active, "
      + "       s.dep_id AS staff_dep, l.designation, "
      + "       ug.reg_number, ug.intake_date, ug.status AS ug_status, ug.batch, ug.dep_id AS ug_dep "
      + "FROM users u "
      + "LEFT JOIN staff s          ON s.staff_id   = u.user_id "
      + "LEFT JOIN lecturer l       ON l.lecture_id = u.user_id "
      + "LEFT JOIN undergraduate ug ON ug.stu_id    = u.user_id "
      + "WHERE u.username = ?";

    @Override
    public User findByUsername(String username) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_USERNAME)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    /** Builds the right subclass depending on the role column. */
    private User mapRow(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role"));
        User user;

        switch (role) {
            case ADMIN -> {
                Admin a = new Admin();
                a.setDepId(rs.getInt("staff_dep"));
                user = a;
            }
            case LECTURER -> {
                Lecturer l = new Lecturer();
                l.setDepId(rs.getInt("staff_dep"));
                l.setDesignation(rs.getString("designation"));
                user = l;
            }
            case TECHNICAL_OFFICER -> {
                TechnicalOfficer t = new TechnicalOfficer();
                t.setDepId(rs.getInt("staff_dep"));
                user = t;
            }
            default -> {
                Undergraduate ug = new Undergraduate();
                ug.setRegNumber(rs.getString("reg_number"));
                ug.setIntakeDate(toLocalDate(rs.getDate("intake_date")));
                ug.setStatus(rs.getString("ug_status"));
                ug.setBatch(rs.getString("batch"));
                ug.setDepId(rs.getInt("ug_dep"));
                user = ug;
            }
        }

        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(role);
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setNic(rs.getString("nic"));
        user.setDob(toLocalDate(rs.getDate("dob")));
        user.setPhone(rs.getString("phone"));
        user.setStreet(rs.getString("street"));
        user.setCity(rs.getString("city"));
        user.setPostalCode(rs.getString("postal_code"));
        user.setCountry(rs.getString("country"));
        user.setProfilePicturePath(rs.getString("profile_picture_path"));
        user.setActive(rs.getBoolean("is_active"));
        return user;
    }

    private static LocalDate toLocalDate(Date d) {
        return d == null ? null : d.toLocalDate();
    }
}
