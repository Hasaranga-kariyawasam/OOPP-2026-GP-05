package com.faculty.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public final class DBConnection {

    private static final String CONFIG_FILE = "db/db.properties";
    private static final Properties props = new Properties();
    private static boolean loaded = false;

    // Utility class - no instances (encapsulation)
    private DBConnection() { }

    private static synchronized void loadConfig() throws SQLException {
        if (loaded) {
            return;
        }
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);
            loaded = true;
        } catch (IOException e) {
            throw new SQLException(
                "Cannot read " + CONFIG_FILE + ". Copy db/db.properties.example "
                + "to db/db.properties and set your password.", e);
        }
    }

    /** Opens a NEW connection. The caller must close it. */
    public static Connection getConnection() throws SQLException {
        loadConfig();
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }

    /** Quick health check used by the start-up screen / DBTest. */
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con.isValid(3);
        } catch (SQLException e) {
            System.err.println("DB connection failed: " + e.getMessage());
            return false;
        }
    }
}
