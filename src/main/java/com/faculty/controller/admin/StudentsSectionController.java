package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminOption;
import com.faculty.model.admin.AdminStudent;
import com.faculty.service.admin.AdminLookupService;
import com.faculty.service.admin.AdminStudentService;
import com.faculty.util.admin.AdminUi;
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

/** Manage Students: list + search + register / update / delete (admin section "students"). UI only. */
public class StudentsSectionController {

    @FXML private TableView<AdminStudent> table;
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
    @FXML private ComboBox<AdminOption> deptBox;
    @FXML private DatePicker intakePicker;

    private final AdminStudentService studentService = new AdminStudentService();
    private final AdminLookupService lookupService = new AdminLookupService();

    private int selectedUserId = 0;

    @FXML
    private void initialize() {
        AdminUi.prepare(table);
        AdminUi.column(table, "Reg. Number", AdminStudent::regNumber);
        AdminUi.column(table, "Name", AdminStudent::fullName);
        AdminUi.column(table, "Username", AdminStudent::username);
        AdminUi.column(table, "Email", AdminStudent::email);
        AdminUi.column(table, "Batch", AdminStudent::batch);
        AdminUi.column(table, "Status", AdminStudent::status);
        AdminUi.column(table, "Department", AdminStudent::departmentName);

        statusBox.getItems().setAll("REGULAR", "REPEAT", "BATCH_MISSED");
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
            List<AdminStudent> rows = studentService.search(AdminUi.text(searchField));
            AdminUi.show(table, rows);
            countLabel.setText(rows.size() + " student(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void fillForm(AdminStudent s) {
        selectedUserId = s.userId();
        regField.setText(s.regNumber());
        firstField.setText(s.firstName());
        lastField.setText(s.lastName());
        usernameField.setText(s.username());
        emailField.setText(s.email());
        batchField.setText(s.batch());
        statusBox.setValue(s.status());
        intakePicker.setValue(s.intakeDate());
        AdminUi.select(deptBox, s.depId());
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

    /** Reads the form into a model object; the service checks it. */
    private AdminStudent readForm() {
        return new AdminStudent(selectedUserId, AdminUi.text(regField), AdminUi.text(firstField),
                AdminUi.text(lastField), AdminUi.text(usernameField), AdminUi.text(emailField),
                AdminUi.text(batchField), statusBox.getValue(), intakePicker.getValue(),
                AdminUi.idOf(deptBox), null);
    }

    @FXML private void handleSearch() { refresh(); }

    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleRegister() {
        try {
            studentService.register(readForm(), passwordField.getText());
            AdminUi.info("Student registered.");
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            studentService.update(readForm(), passwordField.getText());
            AdminUi.info("Student updated.");
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedUserId == 0) {
            AdminUi.warn("Select a student from the table first.");
            return;
        }
        if (!AdminUi.confirm("Delete this student and all their attendance, marks and medical records?")) {
            return;
        }
        try {
            studentService.delete(selectedUserId);
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
