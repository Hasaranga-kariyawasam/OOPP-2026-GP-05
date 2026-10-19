package com.faculty.exception;

/**
 * Thrown by the service layer when something goes wrong that the user cannot fix
 * (database down, file cannot be copied ...). The message is a friendly, non-technical
 * sentence; the technical cause is kept in getCause() for the developer log.
 */
public class ServiceException extends Exception {

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
