package com.faculty.controller.student;

import com.faculty.MainApp;
import com.faculty.model.student.StudentRecords.AttendanceRow;
import com.faculty.model.student.StudentRecords.GradeRow;
import com.faculty.model.student.StudentRecords.NoticeInfo;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.SessionManager;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Student dashboard: header (bell + profile), side menu and overview.
 *
 * Every menu button's id is the name of an FXML file in /fxml/student/sections/
 * (courses, attendance, medical, grades, timetable, notices, profile).
 * Clicking a button loads that file into the content area, so a new student screen
 * only needs one FXML file, one controller and one menu button.
 */
public class StudentDashboardController {

    // header
    @FXML private Label profileName;
    @FXML private Label profileMeta;
    @FXML private Label initialsLabel;
    @FXML private Circle avatarCircle;

    // side menu
    @FXML private VBox navBox;
    @FXML private Button navOverview;

    // overview
    @FXML private Node overviewPane;
    @FXML private StackPane sectionHost;
    @FXML private Label welcomeLabel;
    @FXML private Label dateLabel;
    @FXML private Label statCourses;
    @FXML private Label statAttendance;
    @FXML private Label statCgpa;
    @FXML private Label statNotices;
    @FXML private VBox alertBox;
    @FXML private VBox noticeBox;

    private static StudentDashboardController instance;

    private final StudentService service = new StudentService();

    @FXML
    private void initialize() {
        instance = this;
        loadProfile();
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)));
        loadOverview();
        setActive(navOverview);
        showOverview();
    }

    // ------------------------------------------------------------------ profile

    /** Called by the profile screen after saving so the header shows the new picture / details. */
    public static void refreshProfile() {
        if (instance != null) {
            instance.loadProfile();
        }
    }

    private void loadProfile() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            welcomeLabel.setText("Welcome back!");
            profileName.setText("Guest");
            profileMeta.setText("");
            initialsLabel.setText("?");
            avatarCircle.setFill(Color.web("#111827"));
            return;
        }

        welcomeLabel.setText("Welcome back, " + StudentUi.text(s.getFirstName()) + "!");
        profileName.setText(s.getFullName());
        profileMeta.setText(StudentUi.text(s.getRegNumber()) + "  |  Undergraduate");

        Image photo = StudentUi.loadImage(s.getProfilePicturePath(), 88);
        if (photo != null) {
            avatarCircle.setFill(new ImagePattern(photo));
            initialsLabel.setVisible(false);
        } else {
            avatarCircle.setFill(Color.web("#111827"));
            String f = StudentUi.text(s.getFirstName());
            String l = StudentUi.text(s.getLastName());
            String initials = (f.isEmpty() ? "" : f.substring(0, 1)) + (l.isEmpty() ? "" : l.substring(0, 1));
            initialsLabel.setText(initials.isEmpty() ? "?" : initials.toUpperCase());
            initialsLabel.setVisible(true);
        }
    }

    // ----------------------------------------------------------------- overview

    private void loadOverview() {
        alertBox.getChildren().clear();
        noticeBox.getChildren().clear();

        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        try {
            statCourses.setText(String.valueOf(service.dao().findCourses(s.getUserId()).size()));
            statAttendance.setText(StudentUi.percent(service.overallAttendance(s.getUserId())));

            List<GradeRow> grades = service.dao().findGrades(s.getUserId());
            statCgpa.setText(grades.isEmpty() ? "-" : StudentUi.two(service.gpa(grades)));

            List<NoticeInfo> notices = service.dao().findNotices(s.getDepId());
            statNotices.setText(String.valueOf(notices.size()));
            if (notices.isEmpty()) {
                noticeBox.getChildren().add(mutedLabel("No notices at the moment."));
            }
            for (int i = 0; i < Math.min(3, notices.size()); i++) {
                noticeBox.getChildren().add(noticeItem(notices.get(i)));
            }

            List<AttendanceRow> alerts = service.attendanceAlerts(s.getUserId());
            if (alerts.isEmpty()) {
                Label ok = new Label("Your attendance is 80% or more in every course. Keep it up!");
                ok.getStyleClass().add("stu-alert-ok");
                alertBox.getChildren().add(ok);
            }
            for (AttendanceRow r : alerts) {
                Label l = new Label(r.code() + " " + capitalize(r.component()) + " - "
                        + StudentUi.percent(r.percent()) + " (below 80%)");
                l.getStyleClass().add("stu-alert-bad");
                alertBox.getChildren().add(l);
            }
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }

    private Node noticeItem(NoticeInfo n) {
        Label title = new Label(n.title());
        title.getStyleClass().add("stu-notice-title");
        Label date = new Label(n.date());
        date.getStyleClass().add("stu-note");
        VBox box = new VBox(2, title, date);
        box.getStyleClass().add("stu-notice-item");
        return box;
    }

    private Label mutedLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("stu-muted");
        return l;
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : s.charAt(0) + s.substring(1).toLowerCase(Locale.ROOT);
    }

    // --------------------------------------------------------------- navigation

    @FXML
    private void handleNav(ActionEvent event) {
        Button button = (Button) event.getSource();
        setActive(button);
        open(button.getId(), button.getText());
    }

    @FXML
    private void handleProfile() {
        goTo("profile");
    }

    @FXML
    private void handleBell() {
        goTo("notices");
    }

    /** Quick-access buttons on the overview (ids look like "quick-courses"). */
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
            loadOverview();
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
        URL url = getClass().getResource("/fxml/sections/" + key + ".fxml");
        if (url != null) {
            try {
                content = FXMLLoader.load(url);
            } catch (IOException e) {
                e.printStackTrace();
                Throwable root = e;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                content = placeholder(title, "This section could not be loaded: " + root);
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
        heading.getStyleClass().add("stu-card-title");
        Label text = new Label(message);
        text.getStyleClass().add("stu-muted");
        VBox card = new VBox(8, heading, text);
        card.getStyleClass().add("stu-card");
        return card;
    }

    @FXML
    private void handleLogout() {
        SessionManager.clear();   // SEC-06
        instance = null;
        try {
            MainApp.switchScene("/fxml/login.fxml", "Faculty Management System - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
