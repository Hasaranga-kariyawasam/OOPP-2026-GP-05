package com.faculty.dao.admin;

import com.faculty.model.admin.AdminStaff;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: lecturer and technical officer accounts managed by the admin (role = LECTURER or TECHNICAL_OFFICER). */
public interface AdminStaffDAO {
    List<AdminStaff> search(String role, String text) throws SQLException;
    void insert(String role, AdminStaff staff, String passwordHash) throws SQLException;
    void update(String role, AdminStaff staff, String passwordHash) throws SQLException;
}
