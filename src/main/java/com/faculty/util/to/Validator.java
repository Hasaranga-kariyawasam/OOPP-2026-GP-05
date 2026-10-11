package com.faculty.util.to;

import com.faculty.exception.to.ValidationException;

import java.util.regex.Pattern;

/** Small reusable input checks (SEC-03: validate on the GUI / service side before touching the database). */
public final class Validator {

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^(\\+94|0)\\d{9}$");
    private static final Pattern NIC   = Pattern.compile("^(\\d{9}[VvXx]|\\d{12})$");

    private Validator() { }

    /** Throws when the value is null or only spaces; returns the trimmed value otherwise. */
    public static String required(String fieldName, String value) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " is required.");
        }
        return value.trim();
    }

    /** Returns the trimmed value, or null when it is empty (for optional columns). */
    public static String optional(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    public static void maxLength(String fieldName, String value, int max) throws ValidationException {
        if (value != null && value.length() > max) {
            throw new ValidationException(fieldName + " must be at most " + max + " characters.");
        }
    }

    public static void email(String value) throws ValidationException {
        if (!EMAIL.matcher(value).matches()) {
            throw new ValidationException("Please enter a valid e-mail address.");
        }
    }

    /** Sri Lankan numbers: 0771234567 or +94771234567. Empty is allowed. */
    public static void phone(String value) throws ValidationException {
        if (value != null && !PHONE.matcher(value).matches()) {
            throw new ValidationException("Phone number must look like 0771234567 or +94771234567.");
        }
    }

    /** Old (9 digits + V/X) or new (12 digits) NIC. Empty is allowed. */
    public static void nic(String value) throws ValidationException {
        if (value != null && !NIC.matcher(value).matches()) {
            throw new ValidationException("NIC must be 9 digits + V/X, or 12 digits.");
        }
    }
}
