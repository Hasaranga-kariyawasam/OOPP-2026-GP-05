package com.faculty.service;

import com.faculty.exception.ServiceException;

import java.sql.SQLException;

/** Shared helper for the Technical Officer services: turns a SQLException into a friendly message. */
final class ServiceSupport {

    private ServiceSupport() { }

    /** Logs the technical detail for the developer and returns an exception with a user-friendly message. */
    static ServiceException wrap(String action, SQLException cause) {
        System.err.println("[DB] " + action + " failed: " + cause.getMessage());
        return new ServiceException("Could not " + action + ". The system is temporarily unavailable, "
                                    + "please try again.", cause);
    }
}
