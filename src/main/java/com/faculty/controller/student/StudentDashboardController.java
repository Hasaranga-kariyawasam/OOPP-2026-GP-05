package com.faculty.controller.student;

import com.faculty.MainApp;
import com.faculty.model.User;
import com.faculty.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

/** Controller for the Student dashboard. */
public class StudentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;

    @FXML
    private void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getFullName());
            roleLabel.setText("Role: " + user.getRole());
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();   // SEC-06
        try {
            MainApp.switchScene("/fxml/login.fxml", "Faculty Management System - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
