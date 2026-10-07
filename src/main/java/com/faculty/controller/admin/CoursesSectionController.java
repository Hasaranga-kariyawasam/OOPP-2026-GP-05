package com.faculty.controller.admin;

import com.faculty.model.User;
import com.faculty.service.SessionManager;
import com.faculty.util.admin.AdminDb;
import com.faculty.util.admin.AdminDb.Item;
import com.faculty.util.admin.AdminUi;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Course Management (admin section "courses"). */
public class CoursesSectionController {

    private static final String[] HEADERS = {"Code", "Name", "Theory Cr.", "Practical Cr.", "Semester", "Type", "Lecturer"};
    // hidden: 7 course_id, 8 lecture_id

    @FXML private TableView<ObservableList<String>> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField theoryField;
    @FXML private TextField practicalField;
    @FXML private TextField semesterField;
    @FXML private ComboBox<String> typeBox;
    @FXML private ComboBox<Item> lecturerBox;

    private int selectedId = 0;
    private static final Item NONE = new Item(0, "- No lecturer -");

    @FXML
    private void initialize() {
        typeBox.getItems().setAll("THEORY", "PRACTICAL", "BOTH");
        try {
            lecturerBox.getItems().add(NONE);
            lecturerBox.getItems().addAll(AdminDb.items(
                    "SELECT u.user_id, CONCAT(u.first_name,' ',u.last_name) FROM users u "
                  + "JOIN lecturer l ON l.lecture_id = u.user_id ORDER BY u.first_name"));
        } catch (SQLException e) {
            AdminUi.error(e);
        }
        table.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) fillForm(r);
        });
        clearForm();
        refresh();
    }

    private void refresh() {
        String like = "%" + AdminUi.text(searchField) + "%";
        try {
            List<List<String>> rows = AdminDb.query(
                    "SELECT c.course_code, c.course_name, c.credits_theory, c.credits_practical, c.semester, c.course_type, "
                  + "COALESCE(CONCAT(u.first_name,' ',u.last_name),'-'), c.course_id, COALESCE(c.lecture_id,'') "
                  + "FROM course c LEFT JOIN users u ON u.user_id = c.lecture_id "
                  + "WHERE c.course_code LIKE ? OR c.course_name LIKE ? ORDER BY c.course_code", like, like);
            AdminUi.fillTable(table, HEADERS, rows);
            countLabel.setText(rows.size() + " course(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void fillForm(ObservableList<String> r) {
        selectedId = Integer.parseInt(r.get(7));
        codeField.setText(r.get(0));
        nameField.setText(r.get(1));
        theoryField.setText(r.get(2));
        practicalField.setText(r.get(3));
        semesterField.setText(r.get(4));
        typeBox.setValue(r.get(5));
        if (r.get(8).isEmpty()) {
            lecturerBox.setValue(NONE);
        } else {
            AdminUi.select(lecturerBox, r.get(8));
        }
    }

    private void clearForm() {
        selectedId = 0;
        table.getSelectionModel().clearSelection();
        codeField.clear();
        nameField.clear();
        theoryField.setText("3");
        practicalField.setText("0");
        semesterField.clear();
        typeBox.setValue("THEORY");
        lecturerBox.setValue(NONE);
    }

    private Object[] values() {
        String code = AdminUi.text(codeField).toUpperCase();
        if (code.isEmpty() || code.length() > 10) { AdminUi.warn("Course code is required (max 10 characters)."); return null; }
        if (AdminUi.text(nameField).isEmpty()) { AdminUi.warn("Course name is required."); return null; }
        int theory, practical;
        try {
            theory = Integer.parseInt(AdminUi.text(theoryField));
            practical = Integer.parseInt(AdminUi.text(practicalField));
        } catch (NumberFormatException e) {
            AdminUi.warn("Credits must be whole numbers.");
            return null;
        }
        if (theory < 0 || practical < 0 || theory + practical <= 0) { AdminUi.warn("Total credits must be more than 0."); return null; }
        if (AdminUi.text(semesterField).isEmpty()) { AdminUi.warn("Semester is required (e.g. L2S1)."); return null; }
        if (typeBox.getValue() == null) { AdminUi.warn("Choose a course type."); return null; }
        Item lect = lecturerBox.getValue();
        Integer lectId = (lect == null || lect.id() == 0) ? null : lect.id();
        return new Object[]{code, AdminUi.text(nameField), theory, practical, AdminUi.text(semesterField),
                typeBox.getValue(), lectId};
    }

    @FXML private void handleSearch() { refresh(); }
    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleAdd() {
        Object[] v = values();
        if (v == null) return;
        User admin = SessionManager.getCurrentUser();
        Integer adminId = admin == null ? null : admin.getUserId();
        try {
            AdminDb.update("INSERT INTO course (course_code, course_name, credits_theory, credits_practical, semester, "
                    + "course_type, lecture_id, created_by) VALUES (?,?,?,?,?,?,?,?)",
                    v[0], v[1], v[2], v[3], v[4], v[5], v[6], adminId);
            AdminUi.info("Course saved.");
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedId == 0) { AdminUi.warn("Select a course from the table first."); return; }
        Object[] v = values();
        if (v == null) return;
        try {
            AdminDb.update("UPDATE course SET course_code=?, course_name=?, credits_theory=?, credits_practical=?, "
                    + "semester=?, course_type=?, lecture_id=? WHERE course_id=?",
                    v[0], v[1], v[2], v[3], v[4], v[5], v[6], selectedId);
            AdminUi.info("Course updated.");
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedId == 0) { AdminUi.warn("Select a course from the table first."); return; }
        if (!AdminUi.confirm("Delete this course? Its marks, attendance and timetable entries are deleted too.")) return;
        try {
            AdminDb.update("DELETE FROM course WHERE course_id=?", selectedId);
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
