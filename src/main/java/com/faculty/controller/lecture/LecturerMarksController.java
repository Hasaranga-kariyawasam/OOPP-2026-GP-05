package com.faculty.controller.lecture;

import com.faculty.model.lecture.CourseStudent;
import com.faculty.model.lecture.LecturerCourse;
import com.faculty.model.lecture.MarkEntry;
import com.faculty.service.LecturerCourseService;
import com.faculty.service.LecturerMarksService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

/** "Marks" screen: the lecturer enters / updates marks (out of 100) and views them for one student or the whole batch. */
public class LecturerMarksController {

    @FXML private ComboBox<LecturerCourse> courseCombo;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<MarkEntry> table;
    @FXML private TableColumn<MarkEntry, String> colReg;
    @FXML private TableColumn<MarkEntry, String> colName;
    @FXML private TableColumn<MarkEntry, String> colType;
    @FXML private TableColumn<MarkEntry, String> colCategory;
    @FXML private TableColumn<MarkEntry, String> colMark;

    // entry form
    @FXML private ComboBox<CourseStudent> studentCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextField markField;
    @FXML private Button deleteButton;

    private final LecturerCourseService courseService = new LecturerCourseService();
    private final LecturerMarksService marksService = new LecturerMarksService();
    private final ObservableList<MarkEntry> data = FXCollections.observableArrayList();
    private FilteredList<MarkEntry> filtered;
    private int totalCount;

    @FXML
    private void initialize() {
        colReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegNumber()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEvaluationType()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colMark.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMarksValue().toPlainString()));

        filtered = new FilteredList<>(data, m -> true);
        SortedList<MarkEntry> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setPlaceholder(new Label("Select a course to see its marks."));

        courseCombo.setConverter(new StringConverter<>() {
            @Override public String toString(LecturerCourse c) {
                return c == null ? "" : c.getCourseCode() + " - " + c.getCourseName();
            }
            @Override public LecturerCourse fromString(String s) { return null; }
        });
        typeCombo.getItems().addAll("QUIZ1", "QUIZ2", "ASSIGNMENT", "MID", "PRACTICAL", "FINAL");
        categoryCombo.getItems().addAll("CA", "FINAL");
        categoryCombo.setValue("CA");
        deleteButton.setDisable(true);

        courseCombo.valueProperty().addListener((o, a, b) -> loadCourseData());   // before loadCourses(), so the first course loads too
        loadCourses();

        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        table.getSelectionModel().selectedItemProperty().addListener((o, a, m) -> fillForm(m));
        typeCombo.valueProperty().addListener((o, a, b) -> {
            if ("FINAL".equalsIgnoreCase(b)) categoryCombo.setValue("FINAL");
        });
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

    private void loadCourseData() {
        LecturerCourse course = courseCombo.getValue();
        studentCombo.getItems().clear();
        data.clear();
        if (course == null) {
            return;
        }
        try {
            studentCombo.setItems(FXCollections.observableArrayList(marksService.getStudents(course.getCourseId())));
            reloadMarks();
        } catch (SQLException | SecurityException e) {
            System.err.println("Loading marks failed: " + e.getMessage());
            showError("Could not load the marks. Please try again later.");
        }
    }

    private void reloadMarks() throws SQLException {
        LecturerCourse course = courseCombo.getValue();
        List<MarkEntry> marks = marksService.getMarks(course.getCourseId());
        totalCount = marks.size();
        data.setAll(marks);
        applyFilter();
    }

    private void applyFilter() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(m -> text.isEmpty()
                || m.getRegNumber().toLowerCase().contains(text)
                || m.getStudentName().toLowerCase().contains(text));
        countLabel.setText("Showing " + filtered.size() + " of " + totalCount + " marks");
    }

    /** Clicking a row copies it into the form so the lecturer can change the mark. */
    private void fillForm(MarkEntry m) {
        deleteButton.setDisable(m == null);
        if (m == null) {
            return;
        }
        for (CourseStudent s : studentCombo.getItems()) {
            if (s.getStuId() == m.getStuId()) {
                studentCombo.setValue(s);
                break;
            }
        }
        typeCombo.setValue(m.getEvaluationType());
        categoryCombo.setValue(m.getCategory());
        markField.setText(m.getMarksValue().toPlainString());
    }

    @FXML
    private void handleSave() {
        LecturerCourse course = courseCombo.getValue();
        CourseStudent student = studentCombo.getValue();
        if (course == null || student == null) {
            showError("Please choose a course and a student.");
            return;
        }
        String type = typeCombo.getEditor().getText();
        try {
            boolean added = marksService.saveMark(student.getStuId(), course.getCourseId(),
                    type, categoryCombo.getValue(), markField.getText());
            reloadMarks();
            showOk(added ? "Mark added." : "Mark updated.");
            markField.clear();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException | SecurityException e) {
            System.err.println("Saving mark failed: " + e.getMessage());
            showError("Could not save the mark. Please try again later.");
        }
    }

    @FXML
    private void handleDelete() {
        MarkEntry selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        try {
            marksService.deleteMark(selected.getMarkId(), selected.getCourseId());
            reloadMarks();
            handleClear();
            showOk("Mark deleted.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException | SecurityException e) {
            System.err.println("Deleting mark failed: " + e.getMessage());
            showError("Could not delete the mark. Please try again later.");
        }
    }

    @FXML
    private void handleClear() {
        table.getSelectionModel().clearSelection();
        studentCombo.setValue(null);
        typeCombo.setValue(null);
        typeCombo.getEditor().clear();
        categoryCombo.setValue("CA");
        markField.clear();
        deleteButton.setDisable(true);
    }

    private void showError(String text) {
        show(text, "#b91c1c");
    }

    private void showOk(String text) {
        show(text, "#15803d");
    }

    private void show(String text, String color) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }
}
