package com.faculty.controller.student;

import com.faculty.model.student.StudentRecords.CourseInfo;
import com.faculty.model.student.StudentRecords.Material;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/** "My Courses": the courses the student is enrolled in, with the materials of the selected course. */
public class StudentCoursesController {

    @FXML private Label summaryLabel;
    @FXML private TableView<CourseInfo> courseTable;
    @FXML private Label materialTitle;
    @FXML private TableView<Material> materialTable;

    private final StudentService service = new StudentService();

    @FXML
    private void initialize() {
        StudentUi.addColumn(courseTable, "Code", CourseInfo::code);
        StudentUi.addColumn(courseTable, "Course Name", CourseInfo::name);
        StudentUi.addColumn(courseTable, "Type", c -> c.type());
        StudentUi.addColumn(courseTable, "Credits (T/P)", c -> c.creditsTheory() + " / " + c.creditsPractical());
        StudentUi.addColumn(courseTable, "Semester", CourseInfo::semester);
        StudentUi.addColumn(courseTable, "Academic Year", CourseInfo::academicYear);
        StudentUi.addColumn(courseTable, "Lecturer", CourseInfo::lecturer);
        StudentUi.fitColumns(courseTable);

        StudentUi.addColumn(materialTable, "Title", Material::title);
        StudentUi.addColumn(materialTable, "Uploaded", Material::uploadDate);
        StudentUi.addColumn(materialTable, "File", m -> new File(m.filePath()).getName());
        StudentUi.fitColumns(materialTable);

        courseTable.getSelectionModel().selectedItemProperty().addListener((o, old, c) -> showMaterials(c));
        loadCourses();
    }

    private void loadCourses() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        try {
            List<CourseInfo> courses = service.dao().findCourses(s.getUserId());
            courseTable.setItems(FXCollections.observableArrayList(courses));
            int credits = courses.stream().mapToInt(CourseInfo::credits).sum();
            summaryLabel.setText(courses.size() + " course(s)  |  " + credits + " credits  |  select a course to see its materials");
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }

    private void showMaterials(CourseInfo course) {
        if (course == null) {
            materialTable.getItems().clear();
            materialTitle.setText("Course Materials");
            return;
        }
        try {
            materialTable.setItems(FXCollections.observableArrayList(service.dao().findMaterials(course.courseId())));
            materialTitle.setText("Course Materials - " + course.code());
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }
}
