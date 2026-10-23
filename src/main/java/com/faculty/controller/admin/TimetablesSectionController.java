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
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Timetable Management (admin section "timetables"). */
public class TimetablesSectionController {

    private static final String[] HEADERS = {"Course", "Department", "Day", "Start", "End", "Type", "Venue"};
    // hidden: 7 timetable_id, 8 course_id, 9 dep_id

    @FXML private TableView<ObservableList<String>> table;
    @FXML private Label countLabel;
    @FXML private ComboBox<Item> courseBox;
    @FXML private ComboBox<Item> deptBox;
    @FXML private ComboBox<String> dayBox;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField venueField;

    private int selectedId = 0;

    @FXML
    private void initialize() {
        dayBox.getItems().setAll("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
        typeBox.getItems().setAll("THEORY", "PRACTICAL");
        try {
            courseBox.getItems().setAll(AdminDb.items(
                    "SELECT course_id, CONCAT(course_code,' - ',course_name) FROM course ORDER BY course_code"));
            deptBox.getItems().setAll(AdminDb.items("SELECT dep_id, department_name FROM department ORDER BY department_name"));
        } catch (SQLException e) {
            AdminUi.error(e);
        }
        table.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) {
                selectedId = Integer.parseInt(r.get(7));
                AdminUi.select(courseBox, r.get(8));
                AdminUi.select(deptBox, r.get(9));
                dayBox.setValue(r.get(2));
                startField.setText(shortTime(r.get(3)));
                endField.setText(shortTime(r.get(4)));
                typeBox.setValue(r.get(5));
                venueField.setText(r.get(6));
            }
        });
        clearForm();
        refresh();
    }

    private String shortTime(String t) {
        return t.length() >= 5 ? t.substring(0, 5) : t;
    }

    private void refresh() {
        try {
            List<List<String>> rows = AdminDb.query(
                    "SELECT c.course_code, d.department_name, t.day, t.start_time, t.end_time, t.session_type, "
                  + "COALESCE(t.venue,''), t.timetable_id, t.course_id, t.dep_id "
                  + "FROM timetable t JOIN course c ON c.course_id = t.course_id "
                  + "JOIN department d ON d.dep_id = t.dep_id "
                  + "ORDER BY FIELD(t.day,'MON','TUE','WED','THU','FRI','SAT','SUN'), t.start_time");
            AdminUi.fillTable(table, HEADERS, rows);
            countLabel.setText(rows.size() + " session(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void clearForm() {
        selectedId = 0;
        table.getSelectionModel().clearSelection();
        courseBox.getSelectionModel().clearSelection();
        deptBox.getSelectionModel().clearSelection();
        if (!deptBox.getItems().isEmpty()) deptBox.getSelectionModel().selectFirst();
        dayBox.setValue("MON");
        typeBox.setValue("THEORY");
        startField.clear();
        endField.clear();
        venueField.clear();
    }

    /** {course, dep, day, start, end, type, venue} or null after showing a message. */
    private Object[] values() {
        if (courseBox.getValue() == null) { AdminUi.warn("Choose a course."); return null; }
        if (deptBox.getValue() == null) { AdminUi.warn("Choose a department."); return null; }
        LocalTime start, end;
        try {
            start = LocalTime.parse(AdminUi.text(startField));
            end = LocalTime.parse(AdminUi.text(endField));
        } catch (DateTimeParseException e) {
            AdminUi.warn("Enter times like 08:30 (24-hour, HH:mm).");
            return null;
        }
        if (!end.isAfter(start)) { AdminUi.warn("End time must be after start time."); return null; }
        return new Object[]{courseBox.getValue().id(), deptBox.getValue().id(), dayBox.getValue(),
                Time.valueOf(start), Time.valueOf(end), typeBox.getValue(), AdminUi.text(venueField)};
    }

    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleAdd() {
        Object[] v = values();
        if (v == null) return;
        User admin = SessionManager.getCurrentUser();
        Integer adminId = admin == null ? null : admin.getUserId();
        try {
            AdminDb.update("INSERT INTO timetable (course_id, dep_id, day, start_time, end_time, session_type, venue, created_by) "
                    + "VALUES (?,?,?,?,?,?,?,?)", v[0], v[1], v[2], v[3], v[4], v[5], v[6], adminId);
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedId == 0) { AdminUi.warn("Select a session from the table first."); return; }
        Object[] v = values();
        if (v == null) return;
        try {
            AdminDb.update("UPDATE timetable SET course_id=?, dep_id=?, day=?, start_time=?, end_time=?, session_type=?, venue=? "
                    + "WHERE timetable_id=?", v[0], v[1], v[2], v[3], v[4], v[5], v[6], selectedId);
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedId == 0) { AdminUi.warn("Select a session from the table first."); return; }
        if (!AdminUi.confirm("Delete this timetable entry?")) return;
        try {
            AdminDb.update("DELETE FROM timetable WHERE timetable_id=?", selectedId);
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
