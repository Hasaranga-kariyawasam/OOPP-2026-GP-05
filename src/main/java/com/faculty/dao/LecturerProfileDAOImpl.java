package com.faculty.dao;

import com.faculty.config.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

public class LecturerProfileDAOImpl implements LecturerProfileDAO {

    private static final String UPDATE =
        "UPDATE users SET first_name = ?, last_name = ?, email = ?, nic = ?, dob = ?, phone = ?, "
      + "street = ?, city = ?, postal_code = ?, country = ? WHERE user_id = ?";

    @Override
    public void updateProfile(int userId, String firstName, String lastName, String email, String nic,
                              LocalDate dob, String phone, String street, String city,
                              String postalCode, String country) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            setNullable(ps, 4, nic);
            if (dob == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5, Date.valueOf(dob));
            }
            setNullable(ps, 6, phone);
            setNullable(ps, 7, street);
            setNullable(ps, 8, city);
            setNullable(ps, 9, postalCode);
            setNullable(ps, 10, country);
            ps.setInt(11, userId);
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
