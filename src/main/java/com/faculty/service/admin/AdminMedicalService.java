package com.faculty.service.admin;

import com.faculty.dao.admin.AdminMedicalDAO;
import com.faculty.dao.admin.AdminMedicalDAOImpl;
import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminMedicalRow;

import java.sql.SQLException;
import java.util.List;

/** Business rules for the admin medical screen: approve or reject a request. */
public class AdminMedicalService {

    private final AdminMedicalDAO medicalDAO = new AdminMedicalDAOImpl();

    public List<AdminMedicalRow> search(String text) throws SQLException {
        AdminSupport.requireAdmin();
        return medicalDAO.search(AdminSupport.text(text));
    }

    public void approve(int medicalId) throws AdminValidationException, SQLException {
        decide(medicalId, "APPROVED");
    }

    public void reject(int medicalId) throws AdminValidationException, SQLException {
        decide(medicalId, "REJECTED");
    }

    private void decide(int medicalId, String status) throws AdminValidationException, SQLException {
        AdminSupport.requireAdmin();
        AdminSupport.requireSelected(medicalId, "a medical request");
        medicalDAO.updateStatus(medicalId, status);
    }
}
