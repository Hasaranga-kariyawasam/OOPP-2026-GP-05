package com.faculty.controller.student;

import com.faculty.MainApp;
import com.faculty.model.User;
import com.faculty.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class StudentDashboardController {

    @FXML private Label roleLabel;
    @FXML private StackPane contentArea; // දකුණු පැත්තේ වෙනස් වන ප්‍රධාන ප්‍රදේශය සඳහා (අවශ්‍ය නම් FXML එකේ center එකට fx:id එකක් දෙන්න)

    @FXML
    private void initialize() {

        User user = SessionManager.getCurrentUser();
        if (user != null && roleLabel != null) {
            roleLabel.setText("Role: " + user.getRole());
        }
    }


    @FXML
    private void handleNavigation(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String buttonText = clickedButton.getText().trim();

        switch (buttonText) {
            case "Home":
                System.out.println("Home clicked");

                break;

            case "Dashboard":
                System.out.println("Dashboard clicked");

                break;

            case "My courses":
                System.out.println("My courses clicked");
                break;

            case "Course Registration":
                System.out.println("Course Registration clicked");
                break;

            case "Exam Registration":
                System.out.println("Exam Registration clicked");
                break;

            case "View Attendence":
                System.out.println("View Attendance clicked");
                break;

            case "Mark Attendence":
                System.out.println("Mark Attendance clicked");
                break;

            case "Submit Medicals":
                System.out.println("Submit Medicals clicked");
                break;

            case "Notices":
                System.out.println("Notices clicked");
                break;

            case "Time Table":
                System.out.println("Time Table clicked");
                break;

            case "Edit Profile":
                System.out.println("Edit Profile clicked");
                break;

            default:
                System.out.println("Unknown action");
                break;
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        try {
            MainApp.switchScene("/fxml/login.fxml", "Faculty Management System - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}