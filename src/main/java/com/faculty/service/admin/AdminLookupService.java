package com.faculty.service.admin;

import com.faculty.dao.admin.AdminLookupDAO;
import com.faculty.dao.admin.AdminLookupDAOImpl;
import com.faculty.model.admin.AdminOption;

import java.sql.SQLException;
import java.util.List;

/** Lists that fill the combo boxes of the admin screens. */
public class AdminLookupService {

    private final AdminLookupDAO lookupDAO = new AdminLookupDAOImpl();

    public List<AdminOption> departments() throws SQLException {
        AdminSupport.requireAdmin();
        return lookupDAO.departments();
    }

    public List<AdminOption> lecturers() throws SQLException {
        AdminSupport.requireAdmin();
        return lookupDAO.lecturers();
    }

    public List<AdminOption> courses() throws SQLException {
        AdminSupport.requireAdmin();
        return lookupDAO.courses();
    }
}
