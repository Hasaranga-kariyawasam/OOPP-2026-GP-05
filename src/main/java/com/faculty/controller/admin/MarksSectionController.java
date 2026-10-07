package com.faculty.controller.admin;

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

/** Marks Management: view, add, correct and delete individual marks (admin section "marks"). */
public class MarksSectionController {

    private static final String[] HEADERS = {"Reg. Number", "Course", "Evaluation", "Category", "Mark (/100)"};
    // hidden: 5 mark_id, 6 course_id

    @FXML private TableView<ObservableList<String>> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TextField regField;
    @FXML private ComboBox<Item> courseBox;
    @FXML private TextField evalField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextField valueField;

    private int selectedId = 0;

    @FXML
    private void initialize() {
        categoryBox.getItems().setAll("CA", "FINAL");
        try {
            courseBox.getItems().setAll(AdminDb.items(
                    "SELECT course_id, CONCAT(course_code,' - ',course_name) FROM course ORDER BY course_code"));
        } catch (SQLException e) {
            AdminUi.error(e);
        }
        table.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) {
                selectedId = Integer.parseInt(r.get(5));
                regField.setText(r.get(0));
                AdminUi.select(courseBox, r.get(6));
                evalField.setText(r.get(2));
                categoryBox.setValue(r.get(3));
                valueField.setText(r.get(4));
            }
        });
        clearForm();
        refresh();
    }

    private void refresh() {
        String like = "%" + AdminUi.text(searchField) + "%";
        try {
            List<List<String>> rows = AdminDb.query(
                    "SELECT ug.reg_number, c.course_code, m.evaluation_type, m.mark_category, m.marks_value, "
                  + "m.mark_id, m.course_id "
                  + "FROM marks m JOIN undergraduate ug ON ug.stu_id = m.stu_id "
                  + "JOIN course c ON c.course_id = m.course_id "
                  + "WHERE ug.reg_number LIKE ? OR c.course_code LIKE ? "
                  + "ORDER BY ug.reg_number, c.course_code, m.mark_id LIMIT 500", like, like);
            AdminUi.fillTable(table, HEADERS, rows);
            countLabel.setText(rows.size() + " mark(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void clearForm() {
        selectedId = 0;
        table.getSelectionModel().clearSelection();
        regField.clear();
        evalField.clear();
        valueField.clear();
        categoryBox.setValue("CA");
        courseBox.getSelectionModel().clearSelection();
    }

    /** Validates evaluation / mark / course; returns {course id, evaluation, category, value} or null. */
    private Object[] common() {
        if (courseBox.getValue() == null) { AdminUi.warn("Choose a course."); return null; }
        String eval = AdminUi.text(evalField).toUpperCase();
        if (eval.isEmpty()) { AdminUi.warn("Enter the evaluation type (QUIZ1, ASSIGNMENT, MID, FINAL ...)."); return null; }
        double value;
        try {
            value = Double.parseDouble(AdminUi.text(valueField));
        } catch (NumberFormatException e) {
            AdminUi.warn("Mark must be a number.");
            return null;
        }
        if (value < 0 || value > 100) { AdminUi.warn("Mark must be between 0 and 100."); return null; }
        return new Object[]{courseBox.getValue().id(), eval, categoryBox.getValue(), value};
    }

    @FXML private void handleSearch() { refresh(); }
    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleAdd() {
        String reg = AdminUi.text(regField);
        if (reg.isEmpty()) { AdminUi.warn("Enter the student's registration number."); return; }
        Object[] v = common();
        if (v == null) return;
        try {
            List<List<String>> stu = AdminDb.query("SELECT stu_id FROM undergraduate WHERE reg_number=?", reg);
            if (stu.isEmpty()) { AdminUi.warn("No student with registration number " + reg + "."); return; }
            AdminDb.update("INSERT INTO marks (stu_id, course_id, evaluation_type, mark_category, marks_value) "
                    + "VALUES (?,?,?,?,?)", Integer.parseInt(stu.get(0).get(0)), v[0], v[1], v[2], v[3]);
            AdminUi.info("Mark added.");
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedId == 0) { AdminUi.warn("Select a mark from the table first."); return; }
        Object[] v = common();
        if (v == null) return;
        try {
            AdminDb.update("UPDATE marks SET course_id=?, evaluation_type=?, mark_category=?, marks_value=? WHERE mark_id=?",
                    v[0], v[1], v[2], v[3], selectedId);
            AdminUi.info("Mark updated.");
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedId == 0) { AdminUi.warn("Select a mark from the table first."); return; }
        if (!AdminUi.confirm("Delete this mark?")) return;
        try {
            AdminDb.update("DELETE FROM marks WHERE mark_id=?", selectedId);
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
