package com.faculty.dao.admin;

import com.faculty.model.admin.AdminOption;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: id + label lists that fill the admin combo boxes. */
public interface AdminLookupDAO {
    List<AdminOption> departments() throws SQLException;
    List<AdminOption> lecturers() throws SQLException;
    List<AdminOption> courses() throws SQLException;
}
