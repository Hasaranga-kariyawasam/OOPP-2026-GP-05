package com.faculty.util;

import org.mindrot.jbcrypt.BCrypt;

/** One-way password hashing with BCrypt (SEC-01). Plain passwords are never stored or logged. */
public final class PasswordUtil {

    private PasswordUtil() { }

    public static String hash(String plainPassword) {

        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            return false;   // stored value was not a valid BCrypt hash
        }
    }
}
