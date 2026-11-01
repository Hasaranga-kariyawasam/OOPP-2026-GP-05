package com.faculty.controller.lecture;

import com.faculty.model.lecture.AttendanceSummaryRow;
import com.faculty.model.lecture.LecturerCourse;
import com.faculty.service.LecturerAttendanceService;
import com.faculty.service.LecturerCourseService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

/** "Attendance" screen: view-only attendance percentages of the students of one course. */
public class LecturerAttendanceController {

    @FXML private ComboBox<LecturerCourse> courseCombo;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<AttendanceSummaryRow> table;
    @FXML private TableColumn<AttendanceSummaryRow, String> colReg;
    @FXML private TableColumn<AttendanceSummaryRow, String> colName;
    @FXML private TableColumn<AttendanceSummaryRow, String> colTheory;
    @FXML private TableColumn<AttendanceSummaryRow, String> colPractical;
    @FXML private TableColumn<AttendanceSummaryRow, String> colOverall;
    @FXML private TableColumn<AttendanceSummaryRow, String> colWithMedical;
    @FXML private TableColumn<AttendanceSummaryRow, String> colScenario;
    @FXML private TableColumn<AttendanceSummaryRow, String> colEligible;

    private final LecturerCourseService courseService = new LecturerCourseService();
    private final LecturerAttendanceService service = new LecturerAttendanceService();
    private final ObservableList<AttendanceSummaryRow> data = FXCollections.observableArrayList();
    private FilteredList<AttendanceSummaryRow> filtered;
    private int totalCount;

    @FXML
    private void initialize() {
        colReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegNumber()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colTheory.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().hasTheory() ? pct(c.getValue().getTheoryPercent()) : "-"));
        colPractical.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().hasPractical() ? pct(c.getValue().getPracticalPercent()) : "-"));
        colOverall.setCellValueFactory(c -> new SimpleStringProperty(pct(c.getValue().getRawPercent())));
        colWithMedical.setCellValueFactory(c -> new SimpleStringProperty(pct(c.getValue().getEffectivePercent())));
        colScenario.setCellValueFactory(c -> new SimpleStringProperty(service.getScenario(c.getValue())));
        colEligible.setCellValueFactory(c -> new SimpleStringProperty(service.isEligible(c.getValue()) ? "Yes" : "No"));

        filtered = new FilteredList<>(data, r -> true);
        SortedList<AttendanceSummaryRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setPlaceholder(new Label("Select a course to see attendance."));

        courseCombo.setConverter(new StringConverter<>() {
            @Override public String toString(LecturerCourse c) {
                return c == null ? "" : c.getCourseCode() + " - " + c.getCourseName();
            }
            @Override public LecturerCourse fromString(String s) { return null; }
        });

        courseCombo.valueProperty().addListener((o, a, b) -> loadAttendance());   // before loadCourses(), so the first course loads too
        loadCourses();
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
    }

    private String pct(double value) {
        return String.format("%.1f%%", value);
    }

    private void loadCourses() {
        try {
            List<LecturerCourse> courses = courseService.getMyCourses();
            courseCombo.setItems(FXCollections.observableArrayList(courses));
            if (!courses.isEmpty()) {
                courseCombo.setValue(courses.get(0));
            }
        } catch (SQLException | SecurityException e) {      // NFR-04: show a message, never crash
            System.err.println("Loading courses failed: " + e.getMessage());
            showError("Could not load your courses. Please try again later.");
        }
    }

    private void loadAttendance() {
        LecturerCourse course = courseCombo.getValue();
        data.clear();
        totalCount = 0;
        if (course == null) {
            applyFilter();
            return;
        }
        try {
            List<AttendanceSummaryRow> rows = service.getAttendance(course.getCourseId());
            totalCount = rows.size();
            data.setAll(rows);
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        } catch (SQLException | SecurityException e) {
            System.err.println("Loading attendance failed: " + e.getMessage());
            showError("Could not load attendance. Please try again later.");
        }
        applyFilter();
    }

    private void applyFilter() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(r -> text.isEmpty()
                || r.getRegNumber().toLowerCase().contains(text)
                || r.getStudentName().toLowerCase().contains(text));
        countLabel.setText("Showing " + filtered.size() + " of " + totalCount + " students");
    }

    private void showError(String text) {
        messageLabel.setText(text);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
