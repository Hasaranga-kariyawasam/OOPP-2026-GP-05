package com.faculty;

import com.faculty.config.DBConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class DBTest {

    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            System.out.println("Connected to: " + con.getCatalog());

            // Constant query (no user input), so a plain Statement is fine here.
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                rs.next();
                System.out.println("Rows in users table: " + rs.getInt(1));
            }
            System.out.println("DB connection OK");

        } catch (SQLException e) {
            System.err.println("DB connection FAILED: " + e.getMessage());
        }
    }
}
