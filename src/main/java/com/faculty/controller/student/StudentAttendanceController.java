package com.faculty.controller.student;

import com.faculty.model.student.StudentRecords.AttendanceRow;
import com.faculty.model.student.StudentRecords.SessionRow;
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

/**
 * Attendance of the logged-in student: summary per course (theory only, practical only or
 * combined) and the session-by-session details of the selected course.
 */
public class StudentAttendanceController {

    private static final String COMBINED_TEXT = "Combined (Theory + Practical)";
    private static final String THEORY_TEXT = "Theory only";
    private static final String PRACTICAL_TEXT = "Practical only";

    @FXML private ComboBox<String> viewBox;
    @FXML private Label overallLabel;
    @FXML private TableView<AttendanceRow> summaryTable;
    @FXML private Label detailTitle;
    @FXML private TableView<SessionRow> detailTable;

    private final StudentService service = new StudentService();

    @FXML
    private void initialize() {
        viewBox.getItems().setAll(COMBINED_TEXT, THEORY_TEXT, PRACTICAL_TEXT);
        viewBox.setValue(COMBINED_TEXT);
        viewBox.valueProperty().addListener((o, old, v) -> loadSummary());

        StudentUi.addColumn(summaryTable, "Code", AttendanceRow::code);
        StudentUi.addColumn(summaryTable, "Course", AttendanceRow::name);
        StudentUi.addColumn(summaryTable, "Type", AttendanceRow::component);
        StudentUi.addColumn(summaryTable, "Sessions", r -> String.valueOf(r.totalSessions()));
        StudentUi.addColumn(summaryTable, "Present", r -> String.valueOf(r.present()));
        StudentUi.addColumn(summaryTable, "Absent", r -> String.valueOf(r.absent()));
        StudentUi.addColumn(summaryTable, "Medical", r -> String.valueOf(r.approvedMedical()));
        StudentUi.addColumn(summaryTable, "Attendance", r -> StudentUi.percent(r.percent()));
        StudentUi.addStatusColumn(summaryTable, "Status", r -> r.eligible() ? "Eligible" : "Not eligible");
        StudentUi.fitColumns(summaryTable);

        StudentUi.addColumn(detailTable, "Session", r -> String.valueOf(r.sessionNo()));
        StudentUi.addColumn(detailTable, "Component", SessionRow::component);
        StudentUi.addColumn(detailTable, "Date", SessionRow::date);
        StudentUi.addStatusColumn(detailTable, "Status", SessionRow::status);
        StudentUi.addStatusColumn(detailTable, "Medical", SessionRow::medical);
        StudentUi.fitColumns(detailTable);

        summaryTable.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> loadDetails(r));
        loadSummary();
    }

    private String currentView() {
        String v = viewBox.getValue();
        if (THEORY_TEXT.equals(v)) {
            return StudentService.THEORY;
        }
        if (PRACTICAL_TEXT.equals(v)) {
            return StudentService.PRACTICAL;
        }
        return StudentService.COMBINED;
    }

    private void loadSummary() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        try {
            summaryTable.setItems(FXCollections.observableArrayList(
                    service.attendanceSummary(s.getUserId(), currentView())));
            overallLabel.setText("Overall attendance: " + StudentUi.percent(service.overallAttendance(s.getUserId())));
            detailTable.getItems().clear();
            detailTitle.setText("Session Details (select a course above)");
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }

    private void loadDetails(AttendanceRow row) {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null || row == null) {
            return;
        }
        try {
            List<SessionRow> sessions = service.dao().findSessions(s.getUserId(), row.courseId());
            String view = currentView();
            if (!StudentService.COMBINED.equals(view)) {
                List<SessionRow> filtered = new ArrayList<>();
                for (SessionRow r : sessions) {
                    if (r.component().equals(view)) {
                        filtered.add(r);
                    }
                }
                sessions = filtered;
            }
            detailTable.setItems(FXCollections.observableArrayList(sessions));
            detailTitle.setText("Session Details - " + row.code() + " " + row.name());
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }
}
