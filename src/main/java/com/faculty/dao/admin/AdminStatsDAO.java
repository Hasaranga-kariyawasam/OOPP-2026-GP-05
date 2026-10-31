package com.faculty.dao.admin;

import com.faculty.model.admin.DashboardStats;

import java.sql.SQLException;

/** ABSTRACTION: the numbers on the admin dashboard. */
public interface AdminStatsDAO {
    DashboardStats load() throws SQLException;
}
