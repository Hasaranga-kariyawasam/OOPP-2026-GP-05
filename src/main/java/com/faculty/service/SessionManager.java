package com.faculty.service;

import com.faculty.model.User;

/** Holds the logged-in user only while they are logged in (SEC-06). */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() { }

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser() { return currentUser; }
    public static boolean isLoggedIn() { return currentUser != null; }
    public static void clear() { currentUser = null; }
}
