package com.faculty.dao.admin;

import com.faculty.model.admin.AdminProfileData;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class AdminUserDAOImpl implements AdminUserDAO {

    @Override
    public void delete(int userId) throws SQLException {
        Jdbc.update("DELETE FROM users WHERE user_id=?", userId);
    }

    @Override
    public String findPasswordHash(int userId) throws SQLException {
        List<String> found = Jdbc.query("SELECT password_hash FROM users WHERE user_id=?",
                rs -> rs.getString(1), userId);
        return found.isEmpty() ? null : found.get(0);
    }

    @Override
    public void updatePassword(int userId, String passwordHash) throws SQLException {
        Jdbc.update("UPDATE users SET password_hash=? WHERE user_id=?", passwordHash, userId);
    }

    @Override
    public void updateProfile(int userId, AdminProfileData p) throws SQLException {
        Jdbc.update("UPDATE users SET first_name=?, last_name=?, email=?, phone=?, nic=?, dob=?, street=?, "
                  + "city=?, postal_code=?, country=?, profile_picture_path=? WHERE user_id=?",
                p.firstName(), p.lastName(), p.email(), p.phone(), p.nic(),
                p.dob() == null ? null : Date.valueOf(p.dob()),
                p.street(), p.city(), p.postalCode(), p.country(), p.picturePath(), userId);
    }
}
