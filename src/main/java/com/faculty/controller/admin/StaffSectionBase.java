package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminOption;
import com.faculty.model.admin.AdminStaff;
import com.faculty.service.admin.AdminLookupService;
import com.faculty.service.admin.AdminStaffService;
import com.faculty.util.admin.AdminUi;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Shared screen logic for the Lecturers and Technical Officers screens. UI only. */
public abstract class StaffSectionBase {

    @FXML protected TableView<AdminStaff> table;
    @FXML protected TextField searchField;
    @FXML protected Label countLabel;
    @FXML protected TextField firstField;
    @FXML protected TextField lastField;
    @FXML protected TextField usernameField;
    @FXML protected TextField emailField;
    @FXML protected PasswordField passwordField;
    @FXML protected ComboBox<AdminOption> deptBox;
    @FXML protected TextField designationField;   // only present on the lecturer screen

    private final AdminStaffService staffService = new AdminStaffService();
    private final AdminLookupService lookupService = new AdminLookupService();

    private int selectedUserId = 0;

    /** LECTURER or TECHNICAL_OFFICER. */
    protected abstract String role();

    /** Word shown in messages, e.g. "Lecturer". */
    protected abstract String label();

    private boolean hasDesignation() {
        return designationField != null;
    }

    @FXML
    public void initialize() {
        AdminUi.prepare(table);
        AdminUi.column(table, "Username", AdminStaff::username);
        AdminUi.column(table, "Name", AdminStaff::fullName);
        AdminUi.column(table, "Email", AdminStaff::email);
        AdminUi.column(table, "Department", AdminStaff::departmentName);
        if (hasDesignation()) {
            AdminUi.column(table, "Designation", AdminStaff::designation);
        }

        try {
            deptBox.getItems().setAll(lookupService.departments());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, row) -> {
            if (row != null) {
                fillForm(row);
            }
        });
        clearForm();
        refresh();
    }

    private void refresh() {
        try {
            List<AdminStaff> rows = staffService.search(role(), AdminUi.text(searchField));
            AdminUi.show(table, rows);
            countLabel.setText(rows.size() + " " + label().toLowerCase() + "(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void fillForm(AdminStaff s) {
        selectedUserId = s.userId();
        usernameField.setText(s.username());
        firstField.setText(s.firstName());
        lastField.setText(s.lastName());
        emailField.setText(s.email());
        AdminUi.select(deptBox, s.depId());
        if (hasDesignation()) {
            designationField.setText(s.designation());
        }
        passwordField.clear();
    }

    private void clearForm() {
        selectedUserId = 0;
        table.getSelectionModel().clearSelection();
        firstField.clear();
        lastField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        if (hasDesignation()) {
            designationField.clear();
        }
        deptBox.getSelectionModel().clearSelection();
        if (!deptBox.getItems().isEmpty()) {
            deptBox.getSelectionModel().selectFirst();
        }
    }

    private AdminStaff readForm() {
        return new AdminStaff(selectedUserId, AdminUi.text(usernameField), AdminUi.text(firstField),
                AdminUi.text(lastField), AdminUi.text(emailField), AdminUi.idOf(deptBox), null,
                hasDesignation() ? AdminUi.text(designationField) : null);
    }

    @FXML public void handleSearch() { refresh(); }

    @FXML public void handleClear() { clearForm(); }

    @FXML
    public void handleRegister() {
        try {
            staffService.register(role(), readForm(), passwordField.getText());
            AdminUi.info(label() + " registered.");
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    public void handleUpdate() {
        try {
            staffService.update(role(), readForm(), passwordField.getText());
            AdminUi.info(label() + " updated.");
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedUserId == 0) {
            AdminUi.warn("Select a " + label().toLowerCase() + " from the table first.");
            return;
        }
        if (!AdminUi.confirm("Delete this " + label().toLowerCase() + "?")) {
            return;
        }
        try {
            staffService.delete(selectedUserId);
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
