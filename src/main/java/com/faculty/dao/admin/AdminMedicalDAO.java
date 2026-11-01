package com.faculty.dao.admin;

import com.faculty.model.admin.AdminMedicalRow;

import java.sql.SQLException;
import java.util.List;

/** ABSTRACTION: medical requests seen by the admin. */
public interface AdminMedicalDAO {
    List<AdminMedicalRow> search(String text) throws SQLException;
    void updateStatus(int medicalId, String status) throws SQLException;
}
