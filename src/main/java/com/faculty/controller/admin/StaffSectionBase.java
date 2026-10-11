package com.faculty.controller.admin;

import com.faculty.dao.admin.AdminDAO;
import com.faculty.util.admin.AdminDb;
import com.faculty.util.admin.AdminDb.Item;
import com.faculty.util.admin.AdminUi;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Shared logic for the Lecturers and Technical Officers screens. */
public abstract class StaffSectionBase {

    // hidden trailing values of each row: 5 user_id, 6 first, 7 last, 8 dep_id
    @FXML protected TableView<ObservableList<String>> table;
    @FXML protected TextField searchField;
    @FXML protected Label countLabel;
    @FXML protected TextField firstField;
    @FXML protected TextField lastField;
    @FXML protected TextField usernameField;
    @FXML protected TextField emailField;
    @FXML protected PasswordField passwordField;
    @FXML protected ComboBox<Item> deptBox;
    @FXML protected TextField designationField;   // only present on the lecturer screen

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
        try {
            deptBox.getItems().setAll(AdminDb.items("SELECT dep_id, department_name FROM department ORDER BY department_name"));
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
        String like = "%" + AdminUi.text(searchField) + "%";
        try {
            List<List<String>> rows = AdminDb.query(
                    "SELECT u.username, CONCAT(u.first_name,' ',u.last_name), u.email, d.department_name, "
                  + "COALESCE(l.designation,''), u.user_id, u.first_name, u.last_name, s.dep_id "
                  + "FROM users u JOIN staff s ON s.staff_id = u.user_id "
                  + "JOIN department d ON d.dep_id = s.dep_id "
                  + "LEFT JOIN lecturer l ON l.lecture_id = u.user_id "
                  + "WHERE u.role = ? AND (u.username LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ?) "
                  + "ORDER BY u.first_name", role(), like, like, like, like);
            String[] headers = hasDesignation()
                    ? new String[]{"Username", "Name", "Email", "Department", "Designation"}
                    : new String[]{"Username", "Name", "Email", "Department"};
            AdminUi.fillTable(table, headers, rows);
            countLabel.setText(rows.size() + " " + label().toLowerCase() + "(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void fillForm(ObservableList<String> r) {
        selectedUserId = Integer.parseInt(r.get(5));
        usernameField.setText(r.get(0));
        firstField.setText(r.get(6));
        lastField.setText(r.get(7));
        emailField.setText(r.get(2));
        AdminUi.select(deptBox, r.get(8));
        if (hasDesignation()) {
            designationField.setText(r.get(4));
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

    private String validate(boolean creating) {
        if (AdminUi.text(firstField).isEmpty() || AdminUi.text(lastField).isEmpty()) return "First and last name are required.";
        if (AdminUi.text(usernameField).isEmpty()) return "Username is required.";
        if (!AdminUi.validEmail(AdminUi.text(emailField))) return "Enter a valid email address.";
        if (deptBox.getValue() == null) return "Choose a department.";
        String pw = passwordField.getText() == null ? "" : passwordField.getText();
        if (creating && pw.length() < 6) return "Password must be at least 6 characters.";
        if (!creating && !pw.isEmpty() && pw.length() < 6) return "New password must be at least 6 characters (or leave it blank).";
        return null;
    }

    private String designation() {
        return hasDesignation() ? AdminUi.text(designationField) : null;
    }

    @FXML public void handleSearch() { refresh(); }

    @FXML public void handleClear() { clearForm(); }

    @FXML
    public void handleRegister() {
        String err = validate(true);
        if (err != null) { AdminUi.warn(err); return; }
        try {
            AdminDAO.registerStaff(role(), AdminUi.text(firstField), AdminUi.text(lastField), AdminUi.text(usernameField),
                    AdminUi.text(emailField), passwordField.getText(), deptBox.getValue().id(), designation());
            AdminUi.info(label() + " registered.");
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    public void handleUpdate() {
        if (selectedUserId == 0) { AdminUi.warn("Select a " + label().toLowerCase() + " from the table first."); return; }
        String err = validate(false);
        if (err != null) { AdminUi.warn(err); return; }
        try {
            AdminDAO.updateStaff(role(), selectedUserId, AdminUi.text(firstField), AdminUi.text(lastField),
                    AdminUi.text(usernameField), AdminUi.text(emailField), passwordField.getText(),
                    deptBox.getValue().id(), designation());
            AdminUi.info(label() + " updated.");
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    public void handleDelete() {
        if (selectedUserId == 0) { AdminUi.warn("Select a " + label().toLowerCase() + " from the table first."); return; }
        if (!AdminUi.confirm("Delete this " + label().toLowerCase() + "?")) return;
        try {
            AdminDAO.deleteUser(selectedUserId);
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
