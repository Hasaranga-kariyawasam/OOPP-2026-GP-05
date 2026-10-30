package com.faculty.controller.lecture;

import com.faculty.model.lecture.EligibilityRow;
import com.faculty.model.lecture.LecturerCourse;
import com.faculty.service.LecturerCourseService;
import com.faculty.service.LecturerEligibilityService;
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

/** "Eligibility" screen: exam eligibility (CA >= 40% and attendance >= 80%) for one student or the whole batch. */
public class LecturerEligibilityController {

    private static final String ALL = "All students";
    private static final String ELIGIBLE = "Eligible";
    private static final String NOT_ELIGIBLE = "Not eligible";

    @FXML private ComboBox<LecturerCourse> courseCombo;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label summaryLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<EligibilityRow> table;
    @FXML private TableColumn<EligibilityRow, String> colReg;
    @FXML private TableColumn<EligibilityRow, String> colName;
    @FXML private TableColumn<EligibilityRow, String> colCa;
    @FXML private TableColumn<EligibilityRow, String> colAttendance;
    @FXML private TableColumn<EligibilityRow, String> colEligible;
    @FXML private TableColumn<EligibilityRow, String> colRemark;

    private final LecturerCourseService courseService = new LecturerCourseService();
    private final LecturerEligibilityService service = new LecturerEligibilityService();
    private final ObservableList<EligibilityRow> data = FXCollections.observableArrayList();
    private FilteredList<EligibilityRow> filtered;

    @FXML
    private void initialize() {
        colReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegNumber()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colCa.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCaAverage() == null ? "-" : String.format("%.1f%%", c.getValue().getCaAverage())));
        colAttendance.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.1f%%", c.getValue().getAttendancePercent())));
        colEligible.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isEligible() ? "Yes" : "No"));
        colRemark.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRemark()));

        filtered = new FilteredList<>(data, r -> true);
        SortedList<EligibilityRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setPlaceholder(new Label("Select a course to see eligibility."));

        courseCombo.setConverter(new StringConverter<>() {
            @Override public String toString(LecturerCourse c) {
                return c == null ? "" : c.getCourseCode() + " - " + c.getCourseName();
            }
            @Override public LecturerCourse fromString(String s) { return null; }
        });
        statusCombo.getItems().addAll(ALL, ELIGIBLE, NOT_ELIGIBLE);
        statusCombo.setValue(ALL);

        loadCourses();
        courseCombo.valueProperty().addListener((o, a, b) -> loadEligibility());
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        statusCombo.valueProperty().addListener((o, a, b) -> applyFilter());
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

    private void loadEligibility() {
        LecturerCourse course = courseCombo.getValue();
        data.clear();
        if (course != null) {
            try {
                data.setAll(service.getEligibility(course.getCourseId()));
                messageLabel.setVisible(false);
                messageLabel.setManaged(false);
            } catch (SQLException | SecurityException e) {
                System.err.println("Loading eligibility failed: " + e.getMessage());
                showError("Could not load eligibility. Please try again later.");
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String status = statusCombo.getValue();
        filtered.setPredicate(r -> {
            boolean statusOk = status == null || ALL.equals(status)
                    || (ELIGIBLE.equals(status) == r.isEligible());
            boolean textOk = text.isEmpty()
                    || r.getRegNumber().toLowerCase().contains(text)
                    || r.getStudentName().toLowerCase().contains(text);
            return statusOk && textOk;
        });

        long eligible = data.stream().filter(EligibilityRow::isEligible).count();
        summaryLabel.setText(eligible + " eligible, " + (data.size() - eligible) + " not eligible  |  showing "
                + filtered.size() + " of " + data.size());
    }

    private void showError(String text) {
        messageLabel.setText(text);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
