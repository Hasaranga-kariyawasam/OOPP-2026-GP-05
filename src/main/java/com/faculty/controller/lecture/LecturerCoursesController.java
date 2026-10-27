package com.faculty.controller.lecture;

import com.faculty.model.lecture.LecturerCourse;
import com.faculty.service.LecturerCourseService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

/** "My Courses" screen: read-only list of the courses assigned to the logged-in lecturer. */
public class LecturerCoursesController {

    @FXML private Label summaryLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<LecturerCourse> table;
    @FXML private TableColumn<LecturerCourse, String> colCode;
    @FXML private TableColumn<LecturerCourse, String> colName;
    @FXML private TableColumn<LecturerCourse, String> colSemester;
    @FXML private TableColumn<LecturerCourse, String> colType;
    @FXML private TableColumn<LecturerCourse, String> colTheory;
    @FXML private TableColumn<LecturerCourse, String> colPractical;
    @FXML private TableColumn<LecturerCourse, String> colTotal;
    @FXML private TableColumn<LecturerCourse, String> colStudents;

    private final LecturerCourseService service = new LecturerCourseService();

    @FXML
    private void initialize() {
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseCode()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseName()));
        colSemester.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSemester()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeLabel()));
        colTheory.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCreditsTheory())));
        colPractical.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCreditsPractical())));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTotalCredits())));
        colStudents.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getStudentCount())));

        table.setPlaceholder(new Label("No courses are assigned to you yet."));
        loadCourses();
    }

    private void loadCourses() {
        try {
            List<LecturerCourse> courses = service.getMyCourses();
            table.setItems(FXCollections.observableArrayList(courses));

            int credits = 0;
            for (LecturerCourse c : courses) {
                credits += c.getTotalCredits();
            }
            summaryLabel.setText(courses.size() + (courses.size() == 1 ? " course" : " courses")
                    + "  |  " + credits + " total credits");
        } catch (SQLException | SecurityException e) {      // NFR-04: show a message, never crash
            System.err.println("Loading courses failed: " + e.getMessage());
            summaryLabel.setText("");
            messageLabel.setText("Could not load your courses. Please try again later.");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }
}
