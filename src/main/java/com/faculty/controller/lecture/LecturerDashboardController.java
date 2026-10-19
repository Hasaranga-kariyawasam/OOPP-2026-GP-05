package com.faculty.controller.lecture;

import com.faculty.MainApp;
import com.faculty.model.User;
import com.faculty.model.UserRole;
import com.faculty.service.lecturer.LecturerService;
import com.faculty.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controller for the Lecturer dashboard: header (profile), side-panel navigation and overview.
 *
 * To plug a finished screen into a menu item, put its FXML at
 * /fxml/lecture/sections/<key>.fxml  (keys: courses, materials, marks, attendance,
 * medical, results, profile). If the file does not exist yet, a "not built yet" card is shown.
 */
public class LecturerDashboardController {

    // header / profile
    @FXML private Label profileName;
    @FXML private Label profileMeta;
    @FXML private Label initialsLabel;
    @FXML private Circle avatarCircle;

    // side panel
    @FXML private VBox navBox;
    @FXML private Button navOverview;

    // overview
    @FXML private Node overviewPane;
    @FXML private StackPane sectionHost;
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label statCourses;
    @FXML private Label statStudents;
    @FXML private Label statMaterials;

    private final LecturerService lecturerService = new LecturerService();

    @FXML
    private void initialize() {
        loadProfile();
        loadStats();
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)));
        setActive(navOverview);
        showOverview();
    }

    // ------------------------------------------------------------------ profile

    private void loadProfile() {
        User user = SessionManager.getCurrentUser();
        if (user == null) {
            welcomeLabel.setText("Welcome back!");
            profileName.setText("Guest");
            profileMeta.setText("");
            initialsLabel.setText("?");
            avatarCircle.setFill(Color.web("#111827"));
            return;
        }
        welcomeLabel.setText("Welcome back, " + safe(user.getFirstName()) + "!");
        profileName.setText(user.getFullName());
        profileMeta.setText(String.format("LC-%04d  |  %s", user.getUserId(), roleText(user.getRole())));
        initialsLabel.setText(initials(user));
        avatarCircle.setFill(Color.web("#111827"));
    }

    private String roleText(UserRole role) {
        return role == null ? "" : "Lecturer";
    }

    private String initials(User user) {
        String f = safe(user.getFirstName());
        String l = safe(user.getLastName());
        String s = (f.isEmpty() ? "" : f.substring(0, 1)) + (l.isEmpty() ? "" : l.substring(0, 1));
        return s.isEmpty() ? "?" : s.toUpperCase();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // --------------------------------------------------------------- overview stats

    private void loadStats() {
        try {
            statCourses.setText(String.valueOf(lecturerService.getMyCourseCount()));
            statStudents.setText(String.valueOf(lecturerService.getMyStudentCount()));
            statMaterials.setText(String.valueOf(lecturerService.getMyMaterialCount()));
        } catch (SQLException | SecurityException e) {      // NFR-04: never crash on DB errors
            System.err.println("Lecturer stats failed: " + e.getMessage());
            statCourses.setText("-");
            statStudents.setText("-");
            statMaterials.setText("-");
        }
    }

    // ------------------------------------------------------------------ navigation

    @FXML
    private void handleNav(ActionEvent event) {
        Button button = (Button) event.getSource();
        setActive(button);
        open(button.getId(), button.getText());
    }

    /** Click on the header profile: opens the Edit Profile screen. */
    @FXML
    private void handleProfile() {
        goTo("profile");
    }

    /** Quick-access buttons on the overview (their ids look like "quick-marks"). */
    @FXML
    private void handleQuick(ActionEvent event) {
        String id = ((Node) event.getSource()).getId();
        goTo(id.substring("quick-".length()));
    }

    private void goTo(String key) {
        for (Node n : navBox.getChildren()) {
            if (n instanceof Button && key.equals(n.getId())) {
                Button b = (Button) n;
                setActive(b);
                open(key, b.getText());
                return;
            }
        }
    }

    private void open(String key, String title) {
        if ("overview".equals(key)) {
            loadStats();
            showOverview();
        } else {
            showSection(key, title);
        }
    }

    private void setActive(Button active) {
        for (Node n : navBox.getChildren()) {
            n.getStyleClass().remove("active");
        }
        active.getStyleClass().add("active");
    }

    private void showOverview() {
        overviewPane.setVisible(true);
        overviewPane.setManaged(true);
        sectionHost.setVisible(false);
        sectionHost.setManaged(false);
    }

    private void showSection(String key, String title) {
        Node content;
        URL url = getClass().getResource("/fxml/lecture/sections/" + key + ".fxml");
        if (url != null) {
            try {
                content = FXMLLoader.load(url);
            } catch (IOException e) {
                e.printStackTrace();
                content = placeholder(title, "This section could not be loaded.");
            }
        } else {
            content = placeholder(title, "This section is not built yet.");
        }
        sectionHost.getChildren().setAll(content);

        overviewPane.setVisible(false);
        overviewPane.setManaged(false);
        sectionHost.setVisible(true);
        sectionHost.setManaged(true);
    }

    private Node placeholder(String title, String message) {
        Label heading = new Label(title);
        heading.getStyleClass().add("adm-card-title");
        Label text = new Label(message);
        text.getStyleClass().add("adm-muted");
        VBox card = new VBox(8, heading, text);
        card.getStyleClass().add("adm-card");
        return card;
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
