package com.faculty.controller.to;

import com.faculty.MainApp;
import com.faculty.exception.to.ServiceException;
import com.faculty.model.to.ApprovalStatus;
import com.faculty.model.to.MedicalRecord;
import com.faculty.model.to.TimetableEntry;
import com.faculty.model.User;
import com.faculty.model.UserRole;
import com.faculty.model.to.TechnicalOfficer;
import com.faculty.service.to.MedicalService;
import com.faculty.service.to.NoticeService;
import com.faculty.service.SessionManager;
import com.faculty.service.to.TODashboardService;
import com.faculty.service.to.TimetableService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller of the Technical Officer dashboard: header (notices bell + profile), side-panel
 * navigation, overview (stats, today's sessions, pending medicals, calendar).
 *
 * Every menu item opens /fxml/to/sections/<key>.fxml
 * (keys: attendance, medical, notices, timetable, profile) - same pattern as the Admin dashboard.
 */
public class TechnicalOfficerDashboardController {

    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

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
    @FXML private Label statPending;
    @FXML private Label statToday;
    @FXML private Label statRecords;
    @FXML private Label statNotices;
    @FXML private Label daySessionsTitle;
    @FXML private VBox daySessionsBox;
    @FXML private VBox pendingBox;

    // calendar
    @FXML private GridPane calendarGrid;
    @FXML private Label calTitle;
    @FXML private Label calSelected;

    private static TechnicalOfficerDashboardController instance;

    private final TODashboardService dashboardService = new TODashboardService();
    private final TimetableService timetableService = new TimetableService();
    private final MedicalService medicalService = new MedicalService();
    private final NoticeService noticeService = new NoticeService();

    private List<TimetableEntry> timetable = new ArrayList<>();
    private YearMonth shownMonth = YearMonth.now();
    private LocalDate selectedDate = LocalDate.now();

    @FXML
    private void initialize() {
        instance = this;
        loadProfile();
        dateLabel.setText(LocalDate.now().format(LONG_DATE));
        initCalendar();
        setActive(navOverview);
        loadOverview();
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

        Image photo = ToUi.loadImage(user.getProfilePicturePath(), 88);
        if (photo != null) {
            avatarCircle.setFill(new ImagePattern(photo));
            initialsLabel.setVisible(false);
        } else {
            avatarCircle.setFill(Color.web("#111827"));
            initialsLabel.setText(initials(user));
        }
    }

    /** TO-0009 style id (role prefix + user id), same as the Admin header. */
    private String displayId(User user) {
        return String.format("TO-%04d", user.getUserId());
    }

    private String roleText(UserRole role) {
        return role == null ? "" : "Technical Officer";
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

    // ------------------------------------------------------------------ overview data

    private void loadOverview() {
        TechnicalOfficer to = ToUi.currentOfficer();
        if (to == null) {
            return;
        }
        try {
            TODashboardService.Stats stats =
                    dashboardService.loadStats(to.getUserId(), to.getDepId(), LocalDate.now().getDayOfWeek());
            statPending.setText(String.valueOf(stats.pendingMedicals()));
            statToday.setText(String.valueOf(stats.sessionsToday()));
            statRecords.setText(String.valueOf(stats.recordsEntered()));
            statNotices.setText(String.valueOf(stats.notices()));

            timetable = timetableService.getTimetable(to.getDepId());
        } catch (ServiceException e) {
            statPending.setText("-");
            statToday.setText("-");
            statRecords.setText("-");
            statNotices.setText("-");
            timetable = new ArrayList<>();
        }
        renderDaySessions();
        renderPendingMedicals();
    }

    /** Sessions of the department timetable that fall on the day chosen in the calendar. */
    private void renderDaySessions() {
        boolean today = selectedDate.equals(LocalDate.now());
        daySessionsTitle.setText(today ? "Today's Sessions" : "Sessions on " + selectedDate.format(SHORT_DATE));
        daySessionsBox.getChildren().clear();

        List<TimetableEntry> sessions = timetableService.filterByDay(timetable, selectedDate.getDayOfWeek());
        if (sessions.isEmpty()) {
            Label none = new Label("No sessions scheduled for " + selectedDate.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH) + ".");
            none.getStyleClass().add("adm-muted");
            daySessionsBox.getChildren().add(none);
            return;
        }
        for (TimetableEntry t : sessions) {
            Label time = new Label(t.getTimeRange());
            time.getStyleClass().add("to-slot-time");
            time.setMinWidth(104);

            Label title = new Label(t.getCourseCode() + " - " + t.getCourseName());
            title.getStyleClass().add("to-slot-title");
            title.setWrapText(true);
            Label meta = new Label(t.getVenue() == null ? "" : t.getVenue());
            meta.getStyleClass().add("to-slot-meta");
            VBox text = new VBox(2, title, meta);
            HBox.setHgrow(text, Priority.ALWAYS);

            Label type = ToUi.chip(t.getSessionType().getLabel(),
                    t.getSessionType().name().equals("THEORY") ? "info" : "neutral");

            HBox row = new HBox(12, time, text, type);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("to-slot");
            daySessionsBox.getChildren().add(row);
        }
    }

    private void renderPendingMedicals() {
        pendingBox.getChildren().clear();
        try {
            List<MedicalRecord> pending = medicalService.find(ApprovalStatus.PENDING, null);
            if (pending.isEmpty()) {
                Label none = new Label("No pending medicals - everything is up to date.");
                none.getStyleClass().add("adm-muted");
                pendingBox.getChildren().add(none);
                return;
            }
            for (MedicalRecord m : pending.stream().limit(5).toList()) {
                Label who = new Label(m.getRegNumber() + "  " + m.getStudentName());
                who.getStyleClass().add("to-slot-title");
                Label reason = new Label(m.getReason() == null ? "" : m.getReason());
                reason.getStyleClass().add("to-slot-meta");
                VBox text = new VBox(2, who, reason);
                HBox.setHgrow(text, Priority.ALWAYS);

                Label date = new Label(m.getSessionDate().format(SHORT_DATE));
                date.getStyleClass().add("to-slot-meta");

                HBox row = new HBox(12, text, date, ToUi.chip("Pending", "warn"));
                row.setAlignment(Pos.CENTER_LEFT);
                row.getStyleClass().add("to-row");
                pendingBox.getChildren().add(row);
            }
            if (pending.size() > 5) {
                Label more = new Label("+ " + (pending.size() - 5) + " more in Medical Management");
                more.getStyleClass().add("to-hint");
                pendingBox.getChildren().add(more);
            }
        } catch (ServiceException e) {
            Label err = new Label(e.getMessage());
            err.getStyleClass().add("to-msg-err");
            pendingBox.getChildren().add(err);
        }
    }

    // ------------------------------------------------------------------ calendar (same as Admin)

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
                renderDaySessions();
            });
            calendarGrid.add(cell, i % 7, 1 + i / 7);
        }
        calSelected.setText(selectedDate.format(LONG_DATE));
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
        renderDaySessions();
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

    /** The "Pending Medicals" tile opens Medical Management. */
    @FXML
    private void openMedical() {
        goTo("medical");
    }

    /** Quick-access buttons on the overview (their ids look like "quick-attendance"). */
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
        URL url = getClass().getResource("/fxml/to/sections/" + key + ".fxml");
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
