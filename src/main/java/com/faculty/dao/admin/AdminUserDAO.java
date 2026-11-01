package com.faculty.dao.admin;

import com.faculty.model.admin.AdminProfileData;

import java.sql.SQLException;

/** ABSTRACTION: operations on the users table that work for any account. */
public interface AdminUserDAO {

    /** Deletes the users row; subtype rows follow through ON DELETE CASCADE. */
    void delete(int userId) throws SQLException;

    /** @return the stored BCrypt hash, or null when the user does not exist */
    String findPasswordHash(int userId) throws SQLException;

    void updatePassword(int userId, String passwordHash) throws SQLException;

    void updateProfile(int userId, AdminProfileData profile) throws SQLException;
}
