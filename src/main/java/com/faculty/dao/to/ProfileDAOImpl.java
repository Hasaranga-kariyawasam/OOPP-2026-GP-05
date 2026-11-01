package com.faculty.dao;

import com.faculty.config.DBConnection;
import com.faculty.model.ProfileUpdate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ProfileDAOImpl implements ProfileDAO {

    @Override
    public void updateOwnProfile(int userId, ProfileUpdate p, String profilePicturePath) throws SQLException {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, nic = ?, dob = ?, "
                   + "       phone = ?, street = ?, city = ?, postal_code = ?, country = ?, "
                   + "       profile_picture_path = ? "
                   + "WHERE user_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.firstName());
            ps.setString(2, p.lastName());
            ps.setString(3, p.email());
            setNullable(ps, 4, p.nic());
            if (p.dob() == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(p.dob()));
            }
            setNullable(ps, 6, p.phone());
            setNullable(ps, 7, p.street());
            setNullable(ps, 8, p.city());
            setNullable(ps, 9, p.postalCode());
            setNullable(ps, 10, p.country());
            setNullable(ps, 11, profilePicturePath);
            ps.setInt(12, userId);
            ps.executeUpdate();
        }
    }

    private static void setNullable(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }
}
