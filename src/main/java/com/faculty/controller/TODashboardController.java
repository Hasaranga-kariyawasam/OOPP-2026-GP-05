package com.faculty.controller;

import com.faculty.MainApp;
import com.faculty.model.User;
import com.faculty.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TODashboardController {

    @FXML private Label pageTitle;
    @FXML private Label dateLabel;
    @FXML private Label userChipLabel;
    @FXML private Label pendingMedicalsLabel;
    @FXML private Label todaySessionsLabel;
    @FXML private Label recordsLabel;

    @FXML private Button btnDashboard;
    @FXML private Button btnAttendance;
    @FXML private Button btnMedical;
    @FXML private Button btnTimetable;
    @FXML private Button btnNotices;
    @FXML private Button btnProfile;

    @FXML
    private void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            userChipLabel.setText("👤 " + user.getFullName());
        }
        dateLabel.setText(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));

        // Placeholder numbers: Week 3 we will load these from the database
        pendingMedicalsLabel.setText("0");
        todaySessionsLabel.setText("0");
        recordsLabel.setText("0");
    }

    /** Highlights the clicked sidebar button and updates the page title. */
    @FXML
    private void handleNav(ActionEvent event) {
        Button clicked = (Button) event.getSource();

        List<Button> navButtons =
                List.of(btnDashboard, btnAttendance, btnMedical, btnTimetable, btnNotices, btnProfile);
        for (Button b : navButtons) {
            b.getStyleClass().remove("to-nav-active");
        }
        clicked.getStyleClass().add("to-nav-active");

        // strip the emoji, keep only the letters
        pageTitle.setText(clicked.getText().replaceAll("[^\\p{L} ]", "").trim());
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