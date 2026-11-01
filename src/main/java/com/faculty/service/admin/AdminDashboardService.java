package com.faculty.service.admin;

import com.faculty.dao.admin.AdminStatsDAO;
import com.faculty.dao.admin.AdminStatsDAOImpl;
import com.faculty.model.admin.DashboardStats;

import java.sql.SQLException;

/** Numbers for the admin dashboard overview. */
public class AdminDashboardService {

    private final AdminStatsDAO statsDAO = new AdminStatsDAOImpl();

    /** @return the stats, or null when the database could not be read (the screen then shows "-") */
    public DashboardStats loadStats() {
        AdminSupport.requireAdmin();
        try {
            return statsDAO.load();
        } catch (SQLException e) {          // NFR-04: never crash on DB errors
            System.err.println("Dashboard stats failed: " + e.getMessage());
            return null;
        }
    }
}
