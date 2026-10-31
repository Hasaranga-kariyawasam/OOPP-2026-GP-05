package com.faculty.service.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.User;
import com.faculty.model.admin.Admin;
import com.faculty.service.SessionManager;

import java.util.regex.Pattern;

/** Small checks shared by the admin services. */
final class AdminSupport {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private AdminSupport() { }

    /**
     * Every admin service calls this first (SEC-02 / SEC-07): hiding a menu item is not the only
     * protection, so the logged-in user must really be an admin.
     */
    static User requireAdmin() {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Admin)) {
            throw new SecurityException("Only a logged-in admin can do this.");
        }
        return user;
    }

    /** user_id of the logged-in admin (stored as created_by / posted_by). */
    static int currentAdminId() {
        return requireAdmin().getUserId();
    }

    static boolean validEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    /** Trimmed text; null becomes "". */
    static String text(String s) {
        return s == null ? "" : s.trim();
    }

    /** Trimmed text, or null when it is empty (for optional columns). */
    static String blankToNull(String s) {
        String t = text(s);
        return t.isEmpty() ? null : t;
    }

    static void require(boolean ok, String message) throws AdminValidationException {
        if (!ok) {
            throw new AdminValidationException(message);
        }
    }

    /** Password rule: at least 6 characters; when optional, an empty value means "keep the old one". */
    static void checkPassword(String password, boolean required) throws AdminValidationException {
        String pw = password == null ? "" : password;
        if (required) {
            require(pw.length() >= 6, "Password must be at least 6 characters.");
        } else {
            require(pw.isEmpty() || pw.length() >= 6, "New password must be at least 6 characters (or leave it blank).");
        }
    }

    static void requireSelected(int id, String what) throws AdminValidationException {
        require(id > 0, "Select " + what + " from the table first.");
    }
}
