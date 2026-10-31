package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminMedicalRow;
import com.faculty.service.admin.AdminMedicalService;
import com.faculty.util.admin.AdminUi;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Medical Requests: search by student, approve or deny (admin section "medical"). UI only. */
public class MedicalSectionController {

    @FXML private TableView<AdminMedicalRow> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private final AdminMedicalService medicalService = new AdminMedicalService();

    @FXML
    private void initialize() {
        AdminUi.prepare(table);
        AdminUi.column(table, "Reg. Number", AdminMedicalRow::regNumber);
        AdminUi.column(table, "Student", AdminMedicalRow::studentName);
        AdminUi.column(table, "Date Covered", r -> String.valueOf(r.sessionDate()));
        AdminUi.column(table, "Status", AdminMedicalRow::status);
        AdminUi.column(table, "Reason", AdminMedicalRow::reason);
        refresh();
    }

    private void refresh() {
        try {
            List<AdminMedicalRow> rows = medicalService.search(AdminUi.text(searchField));
            AdminUi.show(table, rows);
            countLabel.setText("Found " + rows.size() + " record(s).");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML private void handleSearch() { refresh(); }

    @FXML
    private void handleApprove() {
        AdminMedicalRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) { AdminUi.warn("Select a medical request first."); return; }
        try {
            medicalService.approve(row.medicalId());
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDeny() {
        AdminMedicalRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) { AdminUi.warn("Select a medical request first."); return; }
        try {
            medicalService.reject(row.medicalId());
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
