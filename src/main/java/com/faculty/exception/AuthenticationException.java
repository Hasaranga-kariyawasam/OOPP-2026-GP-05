package com.faculty.exception;

/** Thrown when login fails (wrong credentials or deactivated account). */
public class AuthenticationException extends Exception {

    public AuthenticationException(String message) {
        super(message);
    }
}
