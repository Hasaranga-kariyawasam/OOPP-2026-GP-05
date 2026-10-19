package com.faculty.service;

import com.faculty.dao.UserDAO;
import com.faculty.dao.UserDAOImpl;
import com.faculty.exception.AuthenticationException;
import com.faculty.model.User;
import com.faculty.util.PasswordUtil;

import java.sql.SQLException;

/** Login business rules (FR-01, FR-03). */
public class AuthService {

    private final UserDAO userDAO = new UserDAOImpl();

    public User login(String username, String password) throws AuthenticationException, SQLException {
        User user = userDAO.findByUsername(username);

        // Same message for "no such user" and "wrong password" so attackers learn nothing.
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password.");
        }
        if (!user.isActive()) {
            throw new AuthenticationException("This account is deactivated. Contact the administrator.");
        }
        return user;
    }

    public void logout() {
        SessionManager.clear();
    }
}
