package com.faculty.controller.student;


import com.faculty.config.DBConnection;
import com.faculty.model.User;
import com.faculty.service.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomePage {

    @FXML private GridPane coursesGrid;

    @FXML
    public void initialize() {
        loadStudentCourses();
    }

    private void loadStudentCourses() {
        User user = SessionManager.getCurrentUser();
        if (user == null || coursesGrid == null) return;

        int studentId = user.getUserId();


        String query = "SELECT c.course_code, c.course_name, c.semester FROM course c " +
                "JOIN student_course sc ON c.course_id = sc.course_id " +
                "WHERE sc.stu_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                coursesGrid.getChildren().clear();

                int col = 0;
                int row = 0;

                while (rs.next()) {
                    String courseCode = rs.getString("course_code");
                    String courseName = rs.getString("course_name");
                    String semester = rs.getString("semester");


                    VBox courseCard = createCourseCard(courseCode, courseName, semester);

                    coursesGrid.add(courseCard, col, row);

                    col++;
                    if (col == 3) { 
                        col = 0;
                        row++;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to load student courses: " + e.getMessage());
        }
    }

    private VBox createCourseCard(String code, String name, String semester) {
        VBox card = new VBox();
        card.getStyleClass().add("course-card");

        // උඩින් ඇති Banner කොටස
        StackPane banner = new StackPane();
        banner.setPrefHeight(110.5);
        banner.getStyleClass().add("course-banner-yellow"); // අවශ්‍ය නම් වෙනත් CSS class එකක් දෙන්න පුළුවන්

        Label semLabel = new Label(semester != null ? semester : "Semester I");
        semLabel.getStyleClass().add("semester-tag");
        StackPane.setAlignment(semLabel, javafx.geometry.Pos.TOP_LEFT);
        StackPane.setMargin(semLabel, new Insets(10, 0, 0, 10));
        banner.getChildren().add(semLabel);

        // යටින් ඇති නම අඩංගු Content කොටස
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
