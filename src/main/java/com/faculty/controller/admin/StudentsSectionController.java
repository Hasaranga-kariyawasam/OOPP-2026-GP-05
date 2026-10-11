package com.faculty.controller.admin;

import com.faculty.dao.admin.AdminDAO;
import com.faculty.util.admin.AdminDb;
import com.faculty.util.admin.AdminDb.Item;
import com.faculty.util.admin.AdminUi;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Manage Students: list + search + register / update / delete (admin section "students"). */
public class StudentsSectionController {

    private static final String[] HEADERS = {"Reg. Number", "Name", "Username", "Email", "Batch", "Status", "Department"};
    // hidden trailing values of each row: 7 user_id, 8 first, 9 last, 10 intake, 11 dep_id

    @FXML private TableView<ObservableList<String>> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TextField regField;
    @FXML private TextField firstField;
    @FXML private TextField lastField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField batchField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> statusBox;
    @FXML private ComboBox<Item> deptBox;
    @FXML private DatePicker intakePicker;

    private int selectedUserId = 0;

    @FXML
    private void initialize() {
        statusBox.getItems().setAll("REGULAR", "REPEAT", "BATCH_MISSED");
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
                    "SELECT ug.reg_number, CONCAT(u.first_name,' ',u.last_name), u.username, u.email, ug.batch, ug.status, "
                  + "d.department_name, u.user_id, u.first_name, u.last_name, ug.intake_date, ug.dep_id "
                  + "FROM undergraduate ug JOIN users u ON u.user_id = ug.stu_id "
                  + "JOIN department d ON d.dep_id = ug.dep_id "
                  + "WHERE ug.reg_number LIKE ? OR u.first_name LIKE ? OR u.last_name LIKE ? OR u.username LIKE ? "
                  + "ORDER BY ug.reg_number", like, like, like, like);
            AdminUi.fillTable(table, HEADERS, rows);
            countLabel.setText(rows.size() + " student(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void fillForm(ObservableList<String> r) {
        selectedUserId = Integer.parseInt(r.get(7));
        regField.setText(r.get(0));
        firstField.setText(r.get(8));
        lastField.setText(r.get(9));
        usernameField.setText(r.get(2));
        emailField.setText(r.get(3));
        batchField.setText(r.get(4));
        statusBox.setValue(r.get(5));
        intakePicker.setValue(r.get(10).isEmpty() ? null : LocalDate.parse(r.get(10)));
        AdminUi.select(deptBox, r.get(11));
        passwordField.clear();
    }

    private void clearForm() {
        selectedUserId = 0;
        table.getSelectionModel().clearSelection();
        regField.clear();
        firstField.clear();
        lastField.clear();
        usernameField.clear();
        emailField.clear();
        batchField.clear();
        passwordField.clear();
        statusBox.setValue("REGULAR");
        intakePicker.setValue(LocalDate.now());
        deptBox.getSelectionModel().clearSelection();
        if (!deptBox.getItems().isEmpty()) {
            deptBox.getSelectionModel().selectFirst();
        }
    }

    /** Returns an error text, or null when the form is fine. */
    private String validate(boolean creating) {
        if (!AdminUi.text(regField).matches("TG/\\d{4}/\\d{4}")) return "Registration number must look like TG/2023/1753.";
        if (AdminUi.text(firstField).isEmpty() || AdminUi.text(lastField).isEmpty()) return "First and last name are required.";
        if (AdminUi.text(usernameField).isEmpty()) return "Username is required.";
        if (!AdminUi.validEmail(AdminUi.text(emailField))) return "Enter a valid email address.";
        if (AdminUi.text(batchField).isEmpty()) return "Batch is required (e.g. 2023).";
        if (statusBox.getValue() == null) return "Choose a status.";
        if (intakePicker.getValue() == null) return "Choose the intake date.";
        if (deptBox.getValue() == null) return "Choose a department.";
        String pw = passwordField.getText() == null ? "" : passwordField.getText();
        if (creating && pw.length() < 6) return "Password must be at least 6 characters.";
        if (!creating && !pw.isEmpty() && pw.length() < 6) return "New password must be at least 6 characters (or leave it blank).";
        return null;
    }

    @FXML private void handleSearch() { refresh(); }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void handleRegister() {
        String err = validate(true);
        if (err != null) { AdminUi.warn(err); return; }
        try {
            AdminDAO.registerStudent(AdminUi.text(regField), AdminUi.text(firstField), AdminUi.text(lastField),
                    AdminUi.text(usernameField), AdminUi.text(emailField), passwordField.getText(),
                    AdminUi.text(batchField), statusBox.getValue(), intakePicker.getValue(), deptBox.getValue().id());
            AdminUi.info("Student registered.");
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedUserId == 0) { AdminUi.warn("Select a student from the table first."); return; }
        String err = validate(false);
        if (err != null) { AdminUi.warn(err); return; }
        try {
            AdminDAO.updateStudent(selectedUserId, AdminUi.text(regField), AdminUi.text(firstField), AdminUi.text(lastField),
                    AdminUi.text(usernameField), AdminUi.text(emailField), passwordField.getText(),
                    AdminUi.text(batchField), statusBox.getValue(), intakePicker.getValue(), deptBox.getValue().id());
            AdminUi.info("Student updated.");
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedUserId == 0) { AdminUi.warn("Select a student from the table first."); return; }
        if (!AdminUi.confirm("Delete this student and all their attendance, marks and medical records?")) return;
        try {
            AdminDAO.deleteUser(selectedUserId);
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
