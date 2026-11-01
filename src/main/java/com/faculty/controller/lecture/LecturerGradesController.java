package com.faculty.controller.lecture;

import com.faculty.model.lecture.GradeRow;
import com.faculty.model.lecture.LecturerCourse;
import com.faculty.service.LecturerCourseService;
import com.faculty.service.LecturerGradeService;
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

/** "Grades" tab of the Marks screen: final marks and grades of one course, for one student or the whole batch. */
public class LecturerGradesController {

    @FXML private ComboBox<LecturerCourse> courseCombo;
    @FXML private TextField searchField;
    @FXML private Label summaryLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<GradeRow> table;
    @FXML private TableColumn<GradeRow, String> colReg;
    @FXML private TableColumn<GradeRow, String> colName;
    @FXML private TableColumn<GradeRow, String> colCa;
    @FXML private TableColumn<GradeRow, String> colFinalExam;
    @FXML private TableColumn<GradeRow, String> colFinalMark;
    @FXML private TableColumn<GradeRow, String> colGrade;
    @FXML private TableColumn<GradeRow, String> colPoint;

    private final LecturerCourseService courseService = new LecturerCourseService();
    private final LecturerGradeService gradeService = new LecturerGradeService();
    private final ObservableList<GradeRow> data = FXCollections.observableArrayList();
    private FilteredList<GradeRow> filtered;

    @FXML
    private void initialize() {
        colReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegNumber()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colCa.setCellValueFactory(c -> new SimpleStringProperty(num(c.getValue().getCaAverage())));
        colFinalExam.setCellValueFactory(c -> new SimpleStringProperty(num(c.getValue().getFinalExam())));
        colFinalMark.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isGraded() ? num(c.getValue().getFinalMark()) : "Pending"));
        colGrade.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isGraded() ? c.getValue().getGrade() : "-"));
        colPoint.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isGraded() ? String.format("%.1f", c.getValue().getGradePoint()) : "-"));

        filtered = new FilteredList<>(data, r -> true);
        SortedList<GradeRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setPlaceholder(new Label("Select a course to see grades."));

        courseCombo.setConverter(new StringConverter<>() {
            @Override public String toString(LecturerCourse c) {
                return c == null ? "" : c.getCourseCode() + " - " + c.getCourseName();
            }
            @Override public LecturerCourse fromString(String s) { return null; }
        });

        courseCombo.valueProperty().addListener((o, a, b) -> load());   // before loadCourses(), so the first course loads too
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        loadCourses();
    }

    private String num(Double v) {
        return v == null ? "-" : String.format("%.2f", v);
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

    /** Also called when the tab is opened again, so marks typed on the Marks tab are included. */
    @FXML
    public void load() {
        LecturerCourse course = courseCombo.getValue();
        data.clear();
        if (course != null) {
            try {
                data.setAll(gradeService.getCourseGrades(course.getCourseId()));
                messageLabel.setVisible(false);
                messageLabel.setManaged(false);
            } catch (SQLException | SecurityException e) {
                System.err.println("Loading grades failed: " + e.getMessage());
                showError("Could not load grades. Please try again later.");
            }
        }
        applyFilter();
    }

    private void applyFilter() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(r -> text.isEmpty()
                || r.getRegNumber().toLowerCase().contains(text)
                || r.getStudentName().toLowerCase().contains(text));

        long graded = data.stream().filter(GradeRow::isGraded).count();
        double avg = data.stream().filter(GradeRow::isGraded).mapToDouble(GradeRow::getFinalMark).average().orElse(0);
        summaryLabel.setText(graded + " graded, " + (data.size() - graded) + " pending"
                + (graded > 0 ? String.format("  |  average final mark %.2f", avg) : "")
                + "  |  showing " + filtered.size() + " of " + data.size());
    }

    private void showError(String text) {
        messageLabel.setText(text);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
