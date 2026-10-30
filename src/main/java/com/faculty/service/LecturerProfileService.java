package com.faculty.service;

import com.faculty.dao.LecturerProfileDAO;
import com.faculty.dao.LecturerProfileDAOImpl;
import com.faculty.dao.UserDAO;
import com.faculty.dao.UserDAOImpl;
import com.faculty.model.User;
import com.faculty.model.lecture.Lecturer;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

/** "Edit my profile" (FR-06): everything except username and password. */
public class LecturerProfileService {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^(\\+94|0)\\d{9}$");
    private static final Pattern NIC = Pattern.compile("^(\\d{9}[VvXx]|\\d{12})$");

    private final LecturerProfileDAO profileDAO = new LecturerProfileDAOImpl();
    private final UserDAO userDAO = new UserDAOImpl();

    private Lecturer requireLecturer() {
        User user = SessionManager.getCurrentUser();
        if (!(user instanceof Lecturer)) {
            throw new SecurityException("Only a logged-in lecturer can edit his profile.");
        }
        return (Lecturer) user;
    }

    public Lecturer getCurrent() {
        return requireLecturer();
    }

    /**
     * Validates and saves the profile of the logged-in lecturer, then refreshes the session user.
     * Throws IllegalArgumentException with a message that can be shown on the screen.
     */
    public void save(String firstName, String lastName, String email, String nic, LocalDate dob,
                     String phone, String street, String city, String postalCode, String country)
            throws SQLException {
        Lecturer me = requireLecturer();

        String first = required("First name", firstName, 50);
        String last = required("Last name", lastName, 50);
        String mail = required("E-mail", email, 100);
        if (!EMAIL.matcher(mail).matches()) {
            throw new IllegalArgumentException("Please enter a valid e-mail address.");
        }
        String nicValue = optional(nic);
        if (nicValue != null && !NIC.matcher(nicValue).matches()) {
            throw new IllegalArgumentException("NIC must be 9 digits + V/X, or 12 digits.");
        }
        String phoneValue = optional(phone);
        if (phoneValue != null && !PHONE.matcher(phoneValue).matches()) {
            throw new IllegalArgumentException("Phone number must look like 0771234567 or +94771234567.");
        }
        if (dob != null && (dob.isAfter(LocalDate.now().minusYears(18)) || dob.getYear() < 1940)) {
            throw new IllegalArgumentException("Date of birth is not valid (staff must be at least 18 years old).");
        }
        String streetValue = optionalMax("Street", street, 100);
        String cityValue = optionalMax("City", city, 50);
        String postalValue = optionalMax("Postal code", postalCode, 10);
        String countryValue = optionalMax("Country", country, 50);

        try {
            profileDAO.updateProfile(me.getUserId(), first, last, mail, nicValue, dob, phoneValue,
                    streetValue, cityValue, postalValue, countryValue);
        } catch (SQLIntegrityConstraintViolationException e) {
            String msg = String.valueOf(e.getMessage()).toLowerCase(Locale.ROOT);
            throw new IllegalArgumentException(msg.contains("nic")
                    ? "This NIC number is already used by another account."
                    : "This e-mail address is already used by another account.");
        }

        User fresh = userDAO.findByUsername(me.getUsername());
        if (fresh != null) {
            SessionManager.setCurrentUser(fresh);
        }
    }

    private String required(String field, String value, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        String v = value.trim();
        if (v.length() > max) {
            throw new IllegalArgumentException(field + " must be at most " + max + " characters.");
        }
        return v;
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String optionalMax(String field, String value, int max) {
        String v = optional(value);
        if (v != null && v.length() > max) {
            throw new IllegalArgumentException(field + " must be at most " + max + " characters.");
        }
        return v;
    }
}
