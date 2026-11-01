package com.faculty.exception;

/**
 * Thrown when the user typed or selected something that breaks a business rule
 * (empty field, wrong date, duplicate record ...). The message is safe to show on screen.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
