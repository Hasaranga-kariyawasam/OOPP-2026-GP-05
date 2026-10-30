package com.faculty.controller.student;

import com.faculty.model.student.StudentRecords.CourseInfo;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

/** LMS home: course cards for the logged-in student (3 per row). */
public class HomePage {

    @FXML private GridPane coursesGrid;

    private final StudentService service = new StudentService();

    @FXML
    private void initialize() {
        loadStudentCourses();
    }

    private void loadStudentCourses() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null || coursesGrid == null) {
            return;
        }
        try {
            coursesGrid.getChildren().clear();
            int col = 0;
            int row = 0;
            for (CourseInfo c : service.dao().findCourses(s.getUserId())) {
                coursesGrid.add(createCourseCard(c.code(), c.name(), c.semester()), col, row);
                if (++col == 3) {
                    col = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }

    private VBox createCourseCard(String code, String name, String semester) {
        VBox card = new VBox();
        card.getStyleClass().add("course-card");

        StackPane banner = new StackPane();
        banner.setPrefHeight(110.5);
        banner.getStyleClass().add("course-banner-yellow");

        Label semLabel = new Label(semester != null ? semester : "Semester I");
        semLabel.getStyleClass().add("semester-tag");
        StackPane.setAlignment(semLabel, Pos.TOP_LEFT);
        StackPane.setMargin(semLabel, new Insets(10, 0, 0, 10));
        banner.getChildren().add(semLabel);

        VBox contentArea = new VBox();
        contentArea.setSpacing(8);
        contentArea.getStyleClass().add("course-content-area");
        contentArea.setPadding(new Insets(12, 12, 12, 12));

        Label titleLabel = new Label(code + " " + name);
        titleLabel.getStyleClass().add("course-title");
        titleLabel.setWrapText(true);

        contentArea.getChildren().add(titleLabel);
        card.getChildren().addAll(banner, contentArea);
        return card;
    }
}