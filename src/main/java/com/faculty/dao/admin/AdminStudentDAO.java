package com.faculty.dao.admin;

import com.faculty.model.admin.AdminStudent;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: undergraduate accounts managed by the admin. */
public interface AdminStudentDAO {
    List<AdminStudent> search(String text) throws SQLException;

    /** Inserts users + undergraduate in one transaction. */
    void insert(AdminStudent student, String passwordHash) throws SQLException;

    /** passwordHash null/blank = keep the current password. */
    void update(AdminStudent student, String passwordHash) throws SQLException;
}
