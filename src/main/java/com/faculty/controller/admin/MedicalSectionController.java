package com.faculty.controller.admin;

import com.faculty.util.admin.AdminDb;
import com.faculty.util.admin.AdminUi;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Medical Requests: search by student, approve or deny (admin section "medical"). */
public class MedicalSectionController {

    private static final String[] HEADERS = {"Reg. Number", "Student", "Date Covered", "Status", "Reason"};
    // hidden: 5 medical_id

    @FXML private TableView<ObservableList<String>> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML
    private void initialize() {
        refresh();
    }

    private void refresh() {
        String like = "%" + AdminUi.text(searchField) + "%";
        try {
            List<List<String>> rows = AdminDb.query(
                    "SELECT ug.reg_number, CONCAT(u.first_name,' ',u.last_name), m.session_date, m.approval_status, "
                  + "COALESCE(m.reason,''), m.medical_id "
                  + "FROM medical_record m JOIN undergraduate ug ON ug.stu_id = m.stu_id "
                  + "JOIN users u ON u.user_id = ug.stu_id "
                  + "WHERE ug.reg_number LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? "
                  + "ORDER BY m.session_date DESC", like, like, like);
            AdminUi.fillTable(table, HEADERS, rows);
            countLabel.setText("Found " + rows.size() + " record(s).");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML private void handleSearch() { refresh(); }

    @FXML private void handleApprove() { decide("APPROVED"); }

    @FXML private void handleDeny() { decide("REJECTED"); }

    private void decide(String status) {
        ObservableList<String> row = table.getSelectionModel().getSelectedItem();
        if (row == null) { AdminUi.warn("Select a medical request first."); return; }
        try {
            AdminDb.update("UPDATE medical_record SET approval_status=? WHERE medical_id=?",
                    status, Integer.parseInt(row.get(5)));
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
