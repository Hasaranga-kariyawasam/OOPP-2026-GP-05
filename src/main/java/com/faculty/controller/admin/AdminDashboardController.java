package com.faculty.controller.admin;

import com.faculty.MainApp;
import com.faculty.dao.admin.AdminDAO;
import com.faculty.dao.admin.AdminDAOImpl;
import com.faculty.model.User;
import com.faculty.service.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Controller for the Admin dashboard.
 * One window, five "pages" (dashboard, users, courses, timetable, notices) that are
 * switched with the sidebar buttons. All SQL lives in AdminDAO - nothing JDBC here.
 */
public class AdminDashboardController {

    // ---- header / sidebar ----
    @FXML private Label welcomeLabel;
    @FXML private Label roleLabel;
    @FXML private Label sideNameLabel;
    @FXML private Label sideRoleLabel;
    @FXML private Button navDashboard, navUsers, navCourses, navTimetable, navMarks, navNotices;

    // ---- pages ----
    // the Users page is its own module: users.fxml + UserManagementController (fx:include)
    @FXML private UserManagementController usersPaneController;
    @FXML private VBox dashboardPane, usersPane, coursesPane, timetablePane, marksPane, noticesPane;

    // ---- dashboard widgets ----
    @FXML private Label statUsers, statCourses, statSlots, statNotices;
    @FXML private VBox roleBox;
    @FXML private ListView<String> recentNotices;
    @FXML private Label attPercent, attPresent, attAbsent, attTotal;
    @FXML private ProgressBar attBar;
    @FXML private ListView<String> attList;

    // ---- tables ----
    @FXML private TableView<CourseRow> coursesTable;
    @FXML private TableView<TimetableRow> timetableTable;
    @FXML private TableView<NoticeRow> noticesTable;
    @FXML private TableView<MarkRow> marksTable;
    @FXML private TextField marksSearch;

    private final ObservableList<MarkRow> markData = FXCollections.observableArrayList();
    private final FilteredList<MarkRow> markFiltered = new FilteredList<>(markData, r -> true);

    private final AdminDAO dao = new AdminDAOImpl();
    private VBox[] panes;
    private Button[] navButtons;

    /** Lets a dialog's save action throw checked exceptions. */
    private interface Saver { void save() throws SQLException; }

    // =====================================================================
    // start-up
    // =====================================================================
    @FXML
    private void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome back, " + user.getFirstName());
            roleLabel.setText("Signed in as " + user.getRole().name().replace('_', ' ').toLowerCase());
            sideNameLabel.setText(user.getFullName());
            sideRoleLabel.setText(user.getRole().name().replace('_', ' '));
        }

        panes = new VBox[] { dashboardPane, usersPane, coursesPane, timetablePane, marksPane, noticesPane };
        navButtons = new Button[] { navDashboard, navUsers, navCourses, navTimetable, navMarks, navNotices };

        buildTables();
        showDashboard();
    }

    private void buildTables() {
        coursesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        col(coursesTable, "Code", 80, CourseRow::code);
        col(coursesTable, "Course name", 240, CourseRow::name);
        col(coursesTable, "Theory", 60, r -> String.valueOf(r.creditsTheory()));
        col(coursesTable, "Practical", 70, r -> String.valueOf(r.creditsPractical()));
        col(coursesTable, "Semester", 80, CourseRow::semester);
        col(coursesTable, "Type", 90, CourseRow::type);
        col(coursesTable, "Lecturer", 150, r -> nz(r.lecturer()));

        timetableTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        col(timetableTable, "Course", 100, TimetableRow::courseCode);
        col(timetableTable, "Day", 70, TimetableRow::day);
        col(timetableTable, "Start", 80, TimetableRow::start);
        col(timetableTable, "End", 80, TimetableRow::end);
        col(timetableTable, "Type", 100, TimetableRow::sessionType);
        col(timetableTable, "Venue", 140, r -> nz(r.venue()));

        noticesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        col(noticesTable, "Title", 200, NoticeRow::title);
        col(noticesTable, "Message", 280, NoticeRow::content);
        col(noticesTable, "For", 110, r -> r.targetRole().replace('_', ' '));
        col(noticesTable, "Posted", 130, NoticeRow::posted);
        col(noticesTable, "By", 130, r -> nz(r.postedBy()));

        marksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        col(marksTable, "Reg. number", 120, r -> nz(r.regNumber()));
        col(marksTable, "Student", 180, MarkRow::studentName);
        col(marksTable, "Course", 90, MarkRow::courseCode);
        col(marksTable, "Evaluation", 120, MarkRow::evaluationType);
        col(marksTable, "Category", 80, MarkRow::category);
        col(marksTable, "Marks", 70, MarkRow::marks);
        marksTable.setItems(markFiltered);
        marksSearch.textProperty().addListener((o, a, text) -> {
            String q = text == null ? "" : text.trim().toLowerCase();
            markFiltered.setPredicate(r -> q.isEmpty()
                    || nz(r.regNumber()).toLowerCase().contains(q)
                    || r.studentName().toLowerCase().contains(q)
                    || r.courseCode().toLowerCase().contains(q)
                    || r.evaluationType().toLowerCase().contains(q));
        });

        for (TableView<?> t : List.of(coursesTable, timetableTable, marksTable, noticesTable)) {
            t.setPlaceholder(new Label("Nothing here yet"));
        }
    }

    private <T> void col(TableView<T> table, String title, double width, Function<T, String> value) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(value.apply(cd.getValue())));
        c.setPrefWidth(width);
        table.getColumns().add(c);
    }

    // =====================================================================
    // navigation
    // =====================================================================
    @FXML private void showDashboard() { show(0); refreshDashboard(); }
    @FXML private void showUsers()     { show(1); usersPaneController.refresh(); }
    @FXML private void showCourses()   { show(2); refreshCourses(); }
    @FXML private void showTimetable() { show(3); refreshTimetable(); }
    @FXML private void showMarks()     { show(4); refreshMarks(); }
    @FXML private void showNotices()   { show(5); refreshNotices(); }

    private void show(int index) {
        for (int i = 0; i < panes.length; i++) {
            boolean on = (i == index);
            panes[i].setVisible(on);
            panes[i].setManaged(on);
            if (on) {
                if (!navButtons[i].getStyleClass().contains("active")) navButtons[i].getStyleClass().add("active");
            } else {
                navButtons[i].getStyleClass().remove("active");
            }
        }
    }

    // =====================================================================
    // refresh (reload from DB)
    // =====================================================================
    private void refreshDashboard() {
        try {
            statUsers.setText(String.valueOf(dao.countUsers()));
            statCourses.setText(String.valueOf(dao.countCourses()));
            statSlots.setText(String.valueOf(dao.countTimetableSlots()));
            statNotices.setText(String.valueOf(dao.countNotices()));

            // role breakdown rows (keep the first child = the card title)
            roleBox.getChildren().subList(1, roleBox.getChildren().size()).clear();
            Map<String, Integer> byRole = dao.countUsersByRole();
            int userTotal = Math.max(1, byRole.values().stream().mapToInt(Integer::intValue).sum());
            for (Map.Entry<String, Integer> e : byRole.entrySet()) {
                Label name = new Label(e.getKey().replace('_', ' '));
                name.getStyleClass().add("role-name");
                Region gap = new Region();
                HBox.setHgrow(gap, Priority.ALWAYS);
                Label count = new Label(String.valueOf(e.getValue()));
                count.getStyleClass().add("role-count");
                HBox line = new HBox(name, gap, count);
                ProgressBar bar = new ProgressBar((double) e.getValue() / userTotal);
                bar.setMaxWidth(Double.MAX_VALUE);
                roleBox.getChildren().add(new VBox(6, line, bar));
            }

            // attendance summary
            List<AttendanceRow> att = dao.listAttendanceByCourse();
            int total = att.stream().mapToInt(AttendanceRow::total).sum();
            int present = att.stream().mapToInt(AttendanceRow::present).sum();
            attPercent.setText(total == 0 ? "--" : String.format("%.0f%%", 100.0 * present / total));
            attPresent.setText("Present: " + present);
            attAbsent.setText("Absent: " + (total - present));
            attTotal.setText("Total sessions: " + total);
            attBar.setProgress(total == 0 ? 0 : (double) present / total);
            attList.getItems().clear();
            for (AttendanceRow a : att) {
                attList.getItems().add(String.format("%s   -   %.0f%%   (%d / %d)",
                        a.courseCode(), a.percent(), a.present(), a.total()));
            }
            attList.setPlaceholder(new Label("No attendance recorded yet"));

            recentNotices.getItems().clear();
            List<NoticeRow> notices = dao.listNotices();
            for (int i = 0; i < Math.min(6, notices.size()); i++) {
                NoticeRow n = notices.get(i);
                recentNotices.getItems().add(n.title() + "   -   " + n.posted());
            }
            if (notices.isEmpty()) recentNotices.setPlaceholder(new Label("No notices yet"));
        } catch (SQLException e) {
            dbError(e);
        }
    }

    private void refreshCourses() {
        try { coursesTable.setItems(FXCollections.observableArrayList(dao.listCourses())); }
        catch (SQLException e) { dbError(e); }
    }

    private void refreshTimetable() {
        try { timetableTable.setItems(FXCollections.observableArrayList(dao.listTimetable())); }
        catch (SQLException e) { dbError(e); }
    }

    private void refreshMarks() {
        try { markData.setAll(dao.listMarks()); }
        catch (SQLException e) { dbError(e); }
    }

    private void refreshNotices() {
        try { noticesTable.setItems(FXCollections.observableArrayList(dao.listNotices())); }
        catch (SQLException e) { dbError(e); }
    }

    // =====================================================================
    // COURSES
    // =====================================================================
    @FXML
    private void addCourse() {
        List<Option> lecturers = loadOptions(dao::lecturerOptions);
        if (lecturers == null) return;
        lecturers.add(0, new Option(0, "(not assigned)"));

        TextField code = field("e.g. ICT2132");
        TextField name = field("Course name");
        Spinner<Integer> theory = new Spinner<>(0, 6, 2);
        Spinner<Integer> practical = new Spinner<>(0, 6, 1);
        theory.setEditable(true);
        practical.setEditable(true);
        TextField semester = field("e.g. L2S1");
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("THEORY", "PRACTICAL", "BOTH"));
        type.setValue("BOTH");
        ComboBox<Option> lecturer = new ComboBox<>(FXCollections.observableArrayList(lecturers));
        lecturer.getSelectionModel().selectFirst();
        for (Region r : List.of(type, lecturer, theory, practical)) r.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = newGrid();
        int row = 0;
        addRow(grid, row++, "Course code", code);
        addRow(grid, row++, "Course name", name);
        addRow(grid, row++, "Theory credits", theory);
        addRow(grid, row++, "Practical credits", practical);
        addRow(grid, row++, "Semester", semester);
        addRow(grid, row++, "Type", type);
        addRow(grid, row++, "Lecturer", lecturer);

        Supplier<String> validate = () -> {
            if (code.getText().isBlank() || name.getText().isBlank() || semester.getText().isBlank())
                return "Code, name and semester are required.";
            if (code.getText().trim().length() > 10) return "Course code can be at most 10 characters.";
            if (spinnerValue(theory) + spinnerValue(practical) <= 0) return "Total credits must be more than 0.";
            return null;
        };
        Saver saver = () -> dao.addCourse(code.getText().trim().toUpperCase(), name.getText().trim(),
                spinnerValue(theory), spinnerValue(practical), semester.getText().trim().toUpperCase(),
                type.getValue(), lecturer.getValue().id(), currentAdminId());

        if (form("Add Course", grid, validate, saver)) { refreshCourses(); }
    }

    // =====================================================================
    // TIMETABLE
    // =====================================================================
    @FXML
    private void addTimetable() {
        List<Option> courses = loadOptions(dao::courseOptions);
        List<Option> deps = loadOptions(dao::departmentOptions);
        if (courses == null || deps == null) return;
        if (courses.isEmpty()) { info("Add a course first."); return; }
        if (deps.isEmpty()) { info("No department found."); return; }

        ComboBox<Option> course = new ComboBox<>(FXCollections.observableArrayList(courses));
        course.getSelectionModel().selectFirst();
        ComboBox<Option> dep = new ComboBox<>(FXCollections.observableArrayList(deps));
        dep.getSelectionModel().selectFirst();
        ComboBox<String> day = new ComboBox<>(FXCollections.observableArrayList(
                "MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"));
        day.getSelectionModel().selectFirst();
        TextField start = field("HH:mm  e.g. 08:00");
        TextField end = field("HH:mm  e.g. 10:00");
        ComboBox<String> type = new ComboBox<>(FXCollections.observableArrayList("THEORY", "PRACTICAL"));
        type.getSelectionModel().selectFirst();
        TextField venue = field("Venue (optional)");
        for (Region r : List.of(course, dep, day, type)) r.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = newGrid();
        int row = 0;
        addRow(grid, row++, "Course", course);
        addRow(grid, row++, "Department", dep);
        addRow(grid, row++, "Day", day);
        addRow(grid, row++, "Start time", start);
        addRow(grid, row++, "End time", end);
        addRow(grid, row++, "Session type", type);
        addRow(grid, row++, "Venue", venue);

        Supplier<String> validate = () -> {
            try {
                LocalTime s = LocalTime.parse(start.getText().trim());
                LocalTime e = LocalTime.parse(end.getText().trim());
                if (!e.isAfter(s)) return "End time must be after start time.";
            } catch (DateTimeParseException ex) {
                return "Use 24-hour times like 08:00 and 10:00.";
            }
            return null;
        };
        Saver saver = () -> dao.addTimetable(course.getValue().id(), dep.getValue().id(), currentAdminId(),
                day.getValue(), LocalTime.parse(start.getText().trim()), LocalTime.parse(end.getText().trim()),
                type.getValue(), venue.getText());

        if (form("Add Timetable Slot", grid, validate, saver)) { refreshTimetable(); }
    }

    @FXML
    private void deleteTimetable() {
        TimetableRow sel = timetableTable.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Select a slot in the table first."); return; }
        if (!confirm("Delete " + sel.courseCode() + " on " + sel.day() + " " + sel.start() + "?")) return;
        try {
            dao.deleteTimetable(sel.timetableId());
            refreshTimetable();
        } catch (SQLException e) {
            dbError(e);
        }
    }

    // =====================================================================
    // NOTICES
    // =====================================================================
    @FXML
    private void addNotice() {
        List<Option> deps = loadOptions(dao::departmentOptions);
        if (deps == null) return;
        deps.add(0, new Option(0, "All departments"));

        TextField title = field("Notice title");
        TextArea content = new TextArea();
        content.setPromptText("Write the notice...");
        content.setPrefRowCount(5);
        content.setWrapText(true);
        ComboBox<String> target = new ComboBox<>(FXCollections.observableArrayList(
                "ALL", "UNDERGRADUATE", "LECTURER", "TECHNICAL_OFFICER", "ADMIN"));
        target.setValue("ALL");
        ComboBox<Option> dep = new ComboBox<>(FXCollections.observableArrayList(deps));
        dep.getSelectionModel().selectFirst();
        for (Region r : List.of(target, dep)) r.setMaxWidth(Double.MAX_VALUE);

        GridPane grid = newGrid();
        int row = 0;
        addRow(grid, row++, "Title", title);
        addRow(grid, row++, "Message", content);
        addRow(grid, row++, "Send to", target);
        addRow(grid, row++, "Department", dep);

        Supplier<String> validate = () ->
                (title.getText().isBlank() || content.getText().isBlank()) ? "Title and message are required." : null;
        Saver saver = () -> dao.addNotice(title.getText().trim(), content.getText().trim(),
                target.getValue(), currentAdminId(), dep.getValue().id());

        if (form("Add Notice", grid, validate, saver)) { refreshNotices(); }
    }

    @FXML
    private void deleteNotice() {
        NoticeRow sel = noticesTable.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Select a notice in the table first."); return; }
        if (!confirm("Delete notice \"" + sel.title() + "\"?")) return;
        try {
            dao.deleteNotice(sel.noticeId());
            refreshNotices();
        } catch (SQLException e) {
            dbError(e);
        }
    }

    // =====================================================================
    // logout
    // =====================================================================
    @FXML
    private void handleLogout() {
        SessionManager.clear();   // SEC-06
        try {
            MainApp.switchScene("/fxml/login.fxml", "Faculty Management System - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // =====================================================================
    // small helpers
    // =====================================================================
    private int currentAdminId() {
        User me = SessionManager.getCurrentUser();
        return me == null ? 0 : me.getUserId();
    }

    private static String nz(String s) { return s == null ? "" : s; }

    private static int spinnerValue(Spinner<Integer> sp) {
        try {
            // commit text typed by hand
            Integer v = Integer.valueOf(sp.getEditor().getText().trim());
            return Math.max(0, v);
        } catch (NumberFormatException e) {
            return sp.getValue() == null ? 0 : sp.getValue();
        }
    }

    private static TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        return f;
    }

    private static GridPane newGrid() {
        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(10);
        g.setPadding(new Insets(14, 10, 4, 10));
        g.setMinWidth(380);
        return g;
    }

    private static void addRow(GridPane grid, int row, String label, javafx.scene.Node input) {
        Label l = new Label(label);
        grid.add(l, 0, row);
        grid.add(input, 1, row);
        GridPane.setHgrow(input, Priority.ALWAYS);
    }

    @FunctionalInterface
    private interface OptionLoader { List<Option> load() throws SQLException; }

    /** Loads combo-box data; shows an error and returns null if the database fails. */
    private List<Option> loadOptions(OptionLoader loader) {
        try {
            return new java.util.ArrayList<>(loader.load());
        } catch (SQLException e) {
            dbError(e);
            return null;
        }
    }

    /**
     * Shows a glass-styled form dialog. The OK button validates first, then saves; if either
     * fails the dialog stays open and shows the message in red. Returns true if saved.
     */
    private boolean form(String title, GridPane grid, Supplier<String> validate, Saver saver) {
        Label error = new Label();
        error.getStyleClass().add("form-error");
        error.setWrapText(true);
        VBox box = new VBox(10, grid, error);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        if (MainApp.getPrimaryStage() != null) dialog.initOwner(MainApp.getPrimaryStage());
        DialogPane pane = dialog.getDialogPane();
        pane.setContent(box);
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());
        pane.getStyleClass().add("app-dialog");

        boolean[] saved = { false };
        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        ok.setText("Save");
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            String problem = validate.get();
            if (problem != null) {
                error.setText(problem);
                ev.consume();
                return;
            }
            try {
                saver.save();
                saved[0] = true;
            } catch (SQLException ex) {
                // 1062 = duplicate key (username / email / course code ...)
                error.setText(ex.getErrorCode() == 1062
                        ? "That value already exists (duplicate username, email, code or reg. number)."
                        : "Could not save. Please check the data and try again.");
                System.err.println("Admin save failed: " + ex.getMessage());
                ev.consume();
            }
        });

        dialog.showAndWait();
        return saved[0];
    }

    private void info(String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    private void dbError(SQLException e) {
        System.err.println("Admin DB error: " + e.getMessage());
        Alert a = new Alert(Alert.AlertType.ERROR, "Database error. Please try again.", ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
