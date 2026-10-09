package com.faculty.controller.admin;

import com.faculty.MainApp;
import com.faculty.config.DBConnection;
import com.faculty.model.User;
import com.faculty.model.UserRole;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Controller for the Admin dashboard: header (notices bell + profile), side-panel navigation,
 * overview (welcome banner, stats, quick access) and a month calendar.
 *
 * To plug a finished screen into a menu item, put its FXML at
 * /fxml/admin/sections/<key>.fxml  (keys: students, lecturers, tech_officers, courses,
 * medical, attendance, marks, notices, timetables). If the file does not exist yet,
 * a simple "not built yet" card is shown instead.
 */
public class AdminDashboardController {

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
    @FXML private Label statStudents;
    @FXML private Label statCourses;
    @FXML private Label statFaculty;
    @FXML private Label statRegular;
    @FXML private Label statRepeat;
    @FXML private Label statBatchMissed;
    @FXML private Label statAvgGpa;
    @FXML private Label statTopGpa;
    @FXML private Label statLowGpa;
    @FXML private Label statAttendance;
    @FXML private Label statPendingMedical;
    @FXML private Label statNotices;

    // calendar
    @FXML private GridPane calendarGrid;
    @FXML private Label calTitle;
    @FXML private Label calSelected;

    private static AdminDashboardController instance;

    private YearMonth shownMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    @FXML
    private void initialize() {
        instance = this;
        loadProfile();
        loadStats();
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)));
        initCalendar();
        setActive(navOverview);
        showOverview();
    }

    // ------------------------------------------------------------------ profile

    /** Called by the Edit Profile screen after saving so the header shows the new name / picture. */
    public static void refreshProfile() {
        if (instance != null) {
            instance.loadProfile();
        }
    }

    private void loadProfile() {
        initialsLabel.setVisible(true);
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
        profileMeta.setText(displayId(user) + "  |  " + roleText(user.getRole()));

        Image photo = loadImage(user.getProfilePicturePath());
        if (photo != null) {
            avatarCircle.setFill(new ImagePattern(photo));
            initialsLabel.setVisible(false);
        } else {
            avatarCircle.setFill(Color.web("#111827"));
            initialsLabel.setText(initials(user));
        }
    }

    /** Registration number for students, otherwise a role prefix + user id (AD-0001, LC-0007, TO-0003). */
    private String displayId(User user) {
        if (user instanceof Undergraduate) {
            String reg = ((Undergraduate) user).getRegNumber();
            if (reg != null && !reg.isBlank()) {
                return reg;
            }
        }
        String prefix = "US";
        if (user.getRole() != null) {
            switch (user.getRole()) {
                case ADMIN:             prefix = "AD"; break;
                case LECTURER:          prefix = "LC"; break;
                case TECHNICAL_OFFICER: prefix = "TO"; break;
                default:                prefix = "ST"; break;
            }
        }
        return String.format("%s-%04d", prefix, user.getUserId());
    }

    private String roleText(UserRole role) {
        if (role == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : role.name().toLowerCase().split("_")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
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

    /** Accepts a file path or a URL; returns null when there is no usable picture. */
    private Image loadImage(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            File file = new File(path);
            String url = file.exists() ? file.toURI().toString() : path;
            Image img = new Image(url, 88, 88, false, true);
            return img.isError() ? null : img;
        } catch (Exception e) {
            return null;
        }
    }

    // --------------------------------------------------------------- dashboard stats

    private void loadStats() {
        statStudents.setText(count("SELECT COUNT(*) FROM undergraduate"));
        statCourses.setText(count("SELECT COUNT(*) FROM course"));
        statFaculty.setText(count("SELECT COUNT(*) FROM users WHERE role IN ('LECTURER','TECHNICAL_OFFICER')"));

        statRegular.setText(count("SELECT COUNT(*) FROM undergraduate WHERE status = 'REGULAR'"));
        statRepeat.setText(count("SELECT COUNT(*) FROM undergraduate WHERE status = 'REPEAT'"));
        statBatchMissed.setText(count("SELECT COUNT(*) FROM undergraduate WHERE status = 'BATCH_MISSED'"));

        // credit-weighted GPA of every student that has grades
        String gpa = "(SELECT SUM(g.grade_point * (c.credits_theory + c.credits_practical)) "
                   + "/ NULLIF(SUM(c.credits_theory + c.credits_practical), 0) AS gpa "
                   + "FROM grade g JOIN course c ON c.course_id = g.course_id GROUP BY g.stu_id) t";
        statAvgGpa.setText(scalar("SELECT ROUND(AVG(gpa), 2) FROM " + gpa, ""));
        statTopGpa.setText(scalar("SELECT ROUND(MAX(gpa), 2) FROM " + gpa, ""));
        statLowGpa.setText(count("SELECT COUNT(*) FROM " + gpa + " WHERE gpa < 2.0"));
        statAttendance.setText(scalar("SELECT ROUND(100 * SUM(status = 'PRESENT') / COUNT(*), 1) FROM attendance", "%"));
        statPendingMedical.setText(count("SELECT COUNT(*) FROM medical_record WHERE approval_status = 'PENDING'"));
        statNotices.setText(count("SELECT COUNT(*) FROM notice"));
    }

    /** First column of the first row as text (+ suffix); "-" when there is no data. */
    private String scalar(String sql, String suffix) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            String v = rs.next() ? rs.getString(1) : null;
            return v == null ? "-" : v + suffix;
        } catch (SQLException e) {
            System.err.println("Stat query failed: " + e.getMessage());
            return "-";
        }
    }

    private String count(String sql) {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? String.valueOf(rs.getInt(1)) : "0";
        } catch (SQLException e) {
            System.err.println("Stat query failed: " + e.getMessage());
            return "-";
        }
    }

    // --------------------------------------------------------------------- calendar

    private void initCalendar() {
        calendarGrid.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(cc);
        }
        renderCalendar();
    }

    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        calTitle.setText(shownMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + shownMonth.getYear());

        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int c = 0; c < 7; c++) {
            Label dow = new Label(dayNames[c]);
            dow.getStyleClass().add("adm-cal-dow");
            dow.setMaxWidth(Double.MAX_VALUE);
            dow.setAlignment(Pos.CENTER);
            calendarGrid.add(dow, c, 0);
        }

        LocalDate first = shownMonth.atDay(1);
        LocalDate start = first.minusDays(first.getDayOfWeek().getValue() % 7);   // week starts on Sunday
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 42; i++) {                                              // always 6 rows
            final LocalDate date = start.plusDays(i);
            Label cell = new Label(String.valueOf(date.getDayOfMonth()));
            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setAlignment(Pos.CENTER);
            cell.getStyleClass().add("adm-cal-day");
            if (!YearMonth.from(date).equals(shownMonth)) {
                cell.getStyleClass().add("adm-cal-other");
            }
            if (date.equals(selectedDate)) {
                cell.getStyleClass().add("adm-cal-selected");
            }
            if (date.equals(today)) {
                cell.getStyleClass().add("adm-cal-today");
            }
            cell.setOnMouseClicked(e -> {
                selectedDate = date;
                shownMonth = YearMonth.from(date);
                renderCalendar();
            });
            calendarGrid.add(cell, i % 7, 1 + i / 7);
        }

        calSelected.setText(selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)));
    }

    @FXML
    private void calPrev() {
        shownMonth = shownMonth.minusMonths(1);
        renderCalendar();
    }

    @FXML
    private void calNext() {
        shownMonth = shownMonth.plusMonths(1);
        renderCalendar();
    }

    @FXML
    private void calToday() {
        shownMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        renderCalendar();
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

    /** Header bell: opens the Notices section. */
    @FXML
    private void handleNotices() {
        goTo("notices");
    }

    /** Quick-access buttons on the overview (their ids look like "quick-students"). */
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
        URL url = getClass().getResource("/fxml/admin/sections/" + key + ".fxml");
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
