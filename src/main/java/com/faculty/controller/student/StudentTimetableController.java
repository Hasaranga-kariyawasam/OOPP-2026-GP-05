package com.faculty.controller.student;

import com.faculty.model.student.StudentRecords.TimetableRow;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Weekly timetable of the courses the student is enrolled in, with a day filter. */
public class StudentTimetableController {

    private static final String ALL_DAYS = "All days";

    @FXML private ComboBox<String> dayBox;
    @FXML private Label countLabel;
    @FXML private TableView<TimetableRow> timetableTable;

    private final StudentService service = new StudentService();
    private List<TimetableRow> all = new ArrayList<>();

    @FXML
    private void initialize() {
        dayBox.getItems().setAll(ALL_DAYS, "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        dayBox.setValue(ALL_DAYS);
        dayBox.valueProperty().addListener((o, old, v) -> applyFilter());

        StudentUi.addColumn(timetableTable, "Day", TimetableRow::day);
        StudentUi.addColumn(timetableTable, "Time", t -> t.start() + " - " + t.end());
        StudentUi.addColumn(timetableTable, "Code", TimetableRow::code);
        StudentUi.addColumn(timetableTable, "Course", TimetableRow::name);
        StudentUi.addColumn(timetableTable, "Type", TimetableRow::type);
        StudentUi.addColumn(timetableTable, "Venue", TimetableRow::venue);
        StudentUi.fitColumns(timetableTable);

        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        try {
            all = service.dao().findTimetable(s.getUserId());
            applyFilter();
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }

    private void applyFilter() {
        String day = dayBox.getValue();
        List<TimetableRow> shown = new ArrayList<>();
        for (TimetableRow t : all) {
            if (day == null || ALL_DAYS.equals(day) || t.day().equals(day)) {
                shown.add(t);
            }
        }
        timetableTable.setItems(FXCollections.observableArrayList(shown));
        countLabel.setText(shown.size() + " session(s) per week");
    }
}
