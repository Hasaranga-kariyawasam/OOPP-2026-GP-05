package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminOption;
import com.faculty.model.admin.AdminTimetableForm;
import com.faculty.model.admin.AdminTimetableSlot;
import com.faculty.service.admin.AdminLookupService;
import com.faculty.service.admin.AdminTimetableService;
import com.faculty.util.admin.AdminUi;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Timetable Management (admin section "timetables"). UI only. */
public class TimetablesSectionController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private TableView<AdminTimetableSlot> table;
    @FXML private Label countLabel;
    @FXML private ComboBox<AdminOption> courseBox;
    @FXML private ComboBox<AdminOption> deptBox;
    @FXML private ComboBox<String> dayBox;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private ComboBox<String> typeBox;
    @FXML private TextField venueField;

    private final AdminTimetableService timetableService = new AdminTimetableService();
    private final AdminLookupService lookupService = new AdminLookupService();

    private int selectedId = 0;

    @FXML
    private void initialize() {
        AdminUi.prepare(table);
        AdminUi.column(table, "Course", AdminTimetableSlot::courseCode);
        AdminUi.column(table, "Department", AdminTimetableSlot::departmentName);
        AdminUi.column(table, "Day", AdminTimetableSlot::day);
        AdminUi.column(table, "Start", s -> s.startTime().format(TIME_FMT));
        AdminUi.column(table, "End", s -> s.endTime().format(TIME_FMT));
        AdminUi.column(table, "Type", AdminTimetableSlot::sessionType);
        AdminUi.column(table, "Venue", AdminTimetableSlot::venue);

        dayBox.getItems().setAll(AdminTimetableService.DAYS);
        typeBox.getItems().setAll(AdminTimetableService.SESSION_TYPES);
        try {
            courseBox.getItems().setAll(lookupService.courses());
            deptBox.getItems().setAll(lookupService.departments());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
        table.getSelectionModel().selectedItemProperty().addListener((o, old, s) -> {
            if (s != null) {
                selectedId = s.timetableId();
                AdminUi.select(courseBox, s.courseId());
                AdminUi.select(deptBox, s.depId());
                dayBox.setValue(s.day());
                startField.setText(s.startTime().format(TIME_FMT));
                endField.setText(s.endTime().format(TIME_FMT));
                typeBox.setValue(s.sessionType());
                venueField.setText(s.venue());
            }
        });
        clearForm();
        refresh();
    }

    private void refresh() {
        try {
            List<AdminTimetableSlot> rows = timetableService.findAll();
            AdminUi.show(table, rows);
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
        if (!deptBox.getItems().isEmpty()) {
            deptBox.getSelectionModel().selectFirst();
        }
        dayBox.setValue("MON");
        typeBox.setValue("THEORY");
        startField.clear();
        endField.clear();
        venueField.clear();
    }

    private AdminTimetableForm readForm() {
        return new AdminTimetableForm(AdminUi.idOf(courseBox), AdminUi.idOf(deptBox), dayBox.getValue(),
                AdminUi.text(startField), AdminUi.text(endField), typeBox.getValue(), AdminUi.text(venueField));
    }

    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleAdd() {
        try {
            timetableService.add(readForm());
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
            timetableService.update(selectedId, readForm());
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedId == 0) {
            AdminUi.warn("Select a session from the table first.");
            return;
        }
        if (!AdminUi.confirm("Delete this timetable entry?")) {
            return;
        }
        try {
            timetableService.delete(selectedId);
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
