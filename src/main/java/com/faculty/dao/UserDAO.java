package com.faculty.dao;

import com.faculty.model.User;

import java.sql.SQLException;

/** ABSTRACTION: the service layer talks to this interface, not to JDBC. */
public interface UserDAO {

    /** @return the matching user (as the correct subclass), or null if not found. */
    User findByUsername(String username) throws SQLException;
}
