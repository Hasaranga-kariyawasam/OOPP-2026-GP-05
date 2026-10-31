package com.faculty.exception;

/** Thrown by the admin services when the data entered on a screen is not acceptable. The message is shown to the admin. */
public class AdminValidationException extends Exception {

    public AdminValidationException(String message) {
        super(message);
    }
}
