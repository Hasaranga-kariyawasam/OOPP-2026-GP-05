package com.faculty.controller.student;

import com.faculty.model.student.StudentRecords.GradeRow;
import com.faculty.model.student.StudentRecords.SemesterGpa;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.util.List;

/** Grades of the logged-in student with SGPA per semester and CGPA. */
public class StudentGradesController {

    @FXML private Label cgpaLabel;
    @FXML private Label creditsLabel;
    @FXML private Label coursesLabel;
    @FXML private TableView<GradeRow> gradeTable;
    @FXML private TableView<SemesterGpa> sgpaTable;

    private final StudentService service = new StudentService();

    @FXML
    private void initialize() {
        StudentUi.addColumn(gradeTable, "Code", GradeRow::code);
        StudentUi.addColumn(gradeTable, "Course", GradeRow::name);
        StudentUi.addColumn(gradeTable, "Semester", GradeRow::semester);
        StudentUi.addColumn(gradeTable, "Credits", g -> String.valueOf(g.credits()));
        StudentUi.addColumn(gradeTable, "CA %", g -> g.caPercent() < 0 ? "-" : StudentUi.percent(g.caPercent()));
        StudentUi.addColumn(gradeTable, "Final Mark", g -> StudentUi.two(g.finalMark()));
        StudentUi.addColumn(gradeTable, "Grade", GradeRow::grade);
        StudentUi.addColumn(gradeTable, "Points", g -> StudentUi.two(g.gradePoint()));
        StudentUi.fitColumns(gradeTable);

        StudentUi.addColumn(sgpaTable, "Semester", SemesterGpa::semester);
        StudentUi.addColumn(sgpaTable, "Credits", s -> String.valueOf(s.credits()));
        StudentUi.addColumn(sgpaTable, "SGPA", s -> StudentUi.two(s.sgpa()));
        StudentUi.fitColumns(sgpaTable);

        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        try {
            List<GradeRow> grades = service.dao().findGrades(s.getUserId());
            gradeTable.setItems(FXCollections.observableArrayList(grades));
            sgpaTable.setItems(FXCollections.observableArrayList(service.semesterGpas(grades)));
            cgpaLabel.setText(grades.isEmpty() ? "-" : StudentUi.two(service.gpa(grades)));
            creditsLabel.setText(String.valueOf(service.totalCredits(grades)));
            coursesLabel.setText(String.valueOf(grades.size()));
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }
}
