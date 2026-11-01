package com.faculty.dao;

import com.faculty.model.ProfileUpdate;

import java.sql.SQLException;

/**
 * A logged-in user editing HIS / HER OWN profile (FR-13).
 * Username, password, role and active flag are never touched here.
 */
public interface ProfileDAO {

    /**
     * Updates name, e-mail, NIC, date of birth, phone, address and profile picture.
     * @throws java.sql.SQLIntegrityConstraintViolationException when e-mail / NIC belongs to somebody else
     */
    void updateOwnProfile(int userId, ProfileUpdate update, String profilePicturePath) throws SQLException;
}
