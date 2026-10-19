package com.faculty.controller.to.sections;

import com.faculty.controller.to.ToUi;
import com.faculty.exception.ServiceException;
import com.faculty.exception.ValidationException;
import com.faculty.model.Attendance;
import com.faculty.model.AttendanceSheetRow;
import com.faculty.model.AttendanceStatus;
import com.faculty.model.AttendanceSummary;
import com.faculty.model.Course;
import com.faculty.model.SessionType;
import com.faculty.model.StudentCourseAttendance;
import com.faculty.model.student.Undergraduate;
import com.faculty.model.to.TechnicalOfficer;
import com.faculty.service.AttendanceCalculator;
import com.faculty.service.AttendanceService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Attendance screen: mark / maintain (FR-28, FR-29), batch summary (FR-31), individual summary (FR-32). */
public class ToAttendanceController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final String ALL_BATCHES = "All batches";
    private static final String ALL_STUDENTS = "All undergraduates";
    private static final String ELIGIBLE = "Eligible only";
    private static final String NOT_ELIGIBLE = "Not eligible only";

    /** One choice of the "show attendance for" box. type null = theory + practical combined. */
    private record Component(String label, SessionType type) {
        @Override public String toString() { return label; }
    }

    // tabs
    @FXML private ToggleButton tabMark;
    @FXML private ToggleButton tabBatch;
    @FXML private ToggleButton tabStudent;
    @FXML private Node markPane;
    @FXML private Node batchPane;
    @FXML private Node studentPane;

    // mark tab
    @FXML private ComboBox<Course> markCourse;
    @FXML private ComboBox<SessionType> markType;
    @FXML private ComboBox<Integer> markSession;
    @FXML private DatePicker markDate;
    @FXML private Label markHint;
    @FXML private Label editBanner;
    @FXML private TextField sheetSearch;
    @FXML private Label sheetCounts;
    @FXML private TableView<AttendanceSheetRow> sheetTable;
    @FXML private Label markMessage;
    @FXML private Button btnSave;
    @FXML private Button btnDeleteSession;

    // batch tab
    @FXML private ComboBox<Course> batchCourse;
    @FXML private ComboBox<Component> batchType;
    @FXML private ComboBox<String> batchBatch;
    @FXML private ComboBox<String> batchScenario;
    @FXML private Label tileTotal;
    @FXML private Label tileEligible;
    @FXML private Label tileNotEligible;
    @FXML private Label tileAverage;
    @FXML private Label batchMessage;
    @FXML private TableView<AttendanceSummary> batchTable;

    // individual tab
    @FXML private ComboBox<Undergraduate> indStudent;
    @FXML private Label indSummaryLabel;
    @FXML private Label indMessage;
    @FXML private TableView<StudentCourseAttendance> indTable;
    @FXML private Label logTitle;
    @FXML private Label logHint;
    @FXML private TableView<Attendance> logTable;

    private final AttendanceService service = new AttendanceService();
    private final ObservableList<AttendanceSheetRow> sheetRows = FXCollections.observableArrayList();
    private FilteredList<AttendanceSheetRow> filteredRows;
    private Map<Integer, LocalDate> recorded = Map.of();
    private boolean loading;        // stops the listeners from firing while we fill the boxes in code
    private List<AttendanceSummary> batchData = new ArrayList<>();

    @FXML
    private void initialize() {
        setupTabs();
        setupSheetTable();
        setupBatchTable();
        setupIndividualTables();
        loadPickLists();
    }

    // ------------------------------------------------------------------ tabs & pick lists

    private void setupTabs() {
        ToggleGroup group = new ToggleGroup();
        tabMark.setToggleGroup(group);
        tabBatch.setToggleGroup(group);
        tabStudent.setToggleGroup(group);
        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                oldT.setSelected(true);             // one tab is always selected
                return;
            }
            show(markPane, newT == tabMark);
            show(batchPane, newT == tabBatch);
            show(studentPane, newT == tabStudent);
            if (newT == tabBatch) {
                refreshBatch();
            }
        });
    }

    private static void show(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private void loadPickLists() {
        try {
            List<Course> courses = service.getCourses();
            markCourse.setItems(FXCollections.observableArrayList(courses));
            batchCourse.setItems(FXCollections.observableArrayList(courses));

            List<String> batches = new ArrayList<>();
            batches.add(ALL_BATCHES);
            batches.addAll(service.getBatches());
            batchBatch.setItems(FXCollections.observableArrayList(batches));
            batchBatch.getSelectionModel().selectFirst();

            batchScenario.getItems().add(ALL_STUDENTS);
            for (AttendanceCalculator.Scenario s : AttendanceCalculator.Scenario.values()) {
                batchScenario.getItems().add(s.getLabel());
            }
            batchScenario.getItems().addAll(ELIGIBLE, NOT_ELIGIBLE);
            batchScenario.getSelectionModel().selectFirst();

            indStudent.setItems(FXCollections.observableArrayList(service.getStudents()));
            indStudent.setConverter(new StringConverter<>() {
                @Override public String toString(Undergraduate u) {
                    return u == null ? "" : u.getRegNumber() + " - " + u.getFullName();
                }
                @Override public Undergraduate fromString(String s) { return null; }
            });
        } catch (ServiceException e) {
            ToUi.message(markMessage, e.getMessage(), false);
        }

        markSession.setItems(FXCollections.observableArrayList(sessionNumbers()));
        markDate.setValue(LocalDate.now());
        markDate.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isAfter(LocalDate.now()));      // no future dates
            }
        });

        markCourse.valueProperty().addListener((o, a, b) -> onCourseChanged());
        markType.valueProperty().addListener((o, a, b) -> onSessionTargetChanged(true));
        markDate.valueProperty().addListener((o, a, b) -> onSessionTargetChanged(false));
        batchCourse.valueProperty().addListener((o, a, b) -> onBatchCourseChanged());
        batchType.valueProperty().addListener((o, a, b) -> refreshBatch());
        batchBatch.valueProperty().addListener((o, a, b) -> refreshBatch());
        batchScenario.valueProperty().addListener((o, a, b) -> renderBatch());
        indStudent.valueProperty().addListener((o, a, b) -> refreshIndividual());

        btnDeleteSession.setDisable(true);
    }

    private static List<Integer> sessionNumbers() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= AttendanceCalculator.SESSIONS_PER_COMPONENT; i++) {
            list.add(i);
        }
        return list;
    }

    // ------------------------------------------------------------------ MARK / MAINTAIN

    private void setupSheetTable() {
        filteredRows = new FilteredList<>(sheetRows, r -> true);
        sheetTable.setItems(filteredRows);
        sheetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        sheetTable.setPlaceholder(new Label("Select a course, component and date to load the sheet."));

        TableColumn<AttendanceSheetRow, String> reg = ToUi.column("Reg No", 120, AttendanceSheetRow::getRegNumber);
        TableColumn<AttendanceSheetRow, String> name = ToUi.column("Undergraduate", 220, AttendanceSheetRow::getStudentName);

        TableColumn<AttendanceSheetRow, AttendanceStatus> status = new TableColumn<>("Status (click to change)");
        status.setPrefWidth(170);
        status.setSortable(false);
        status.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getStatus()));
        status.setCellFactory(c -> new TableCell<>() {
            private final Button button = new Button();
            {
                button.getStyleClass().add("to-status-btn");
                button.setOnAction(e -> {
                    AttendanceSheetRow row = getTableRow() == null ? null : getTableRow().getItem();
                    if (row == null) {
                        return;
                    }
                    row.setStatus(row.getStatus() == AttendanceStatus.PRESENT
                            ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT);
                    style(row);
                    updateCounts();
                });
            }
            @Override
            protected void updateItem(AttendanceStatus item, boolean empty) {
                super.updateItem(item, empty);
                AttendanceSheetRow row = getTableRow() == null ? null : getTableRow().getItem();
                if (empty || row == null) {
                    setGraphic(null);
                } else {
                    style(row);
                    setGraphic(button);
                }
            }
            private void style(AttendanceSheetRow row) {
                boolean present = row.getStatus() == AttendanceStatus.PRESENT;
                button.setText(row.getStatus().getLabel());
                button.getStyleClass().removeAll("to-status-present", "to-status-absent");
                button.getStyleClass().add(present ? "to-status-present" : "to-status-absent");
            }
        });

        TableColumn<AttendanceSheetRow, String> medical = ToUi.column("Medical on this date", 150,
                r -> r.getMedicalStatus() == null ? "-" : r.getMedicalStatus().getLabel());
        ToUi.chipColumn(medical, ToUi::approvalKind);

        sheetTable.getColumns().addAll(List.of(reg, name, status, medical));

        sheetSearch.textProperty().addListener((o, a, text) -> {
            String q = text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
            filteredRows.setPredicate(r -> q.isEmpty()
                    || r.getRegNumber().toLowerCase(Locale.ROOT).contains(q)
                    || r.getStudentName().toLowerCase(Locale.ROOT).contains(q));
        });
    }

    /** Course changed: offer only the components the course has, then reload. */
    private void onCourseChanged() {
        Course course = markCourse.getValue();
        loading = true;
        markType.getItems().clear();
        if (course != null) {
            if (course.hasTheory()) {
                markType.getItems().add(SessionType.THEORY);
            }
            if (course.hasPractical()) {
                markType.getItems().add(SessionType.PRACTICAL);
            }
            markType.getSelectionModel().selectFirst();
        }
        loading = false;
        onSessionTargetChanged(true);
    }

    /** Course / component / date changed: reload recorded sessions and the sheet. */
    private void onSessionTargetChanged(boolean suggestNumber) {
        if (loading) {
            return;
        }
        Course course = markCourse.getValue();
        SessionType type = markType.getValue();
        LocalDate date = markDate.getValue();
        ToUi.message(markMessage, null, true);
        sheetRows.clear();
        updateCounts();
        show(editBanner, false);
        btnDeleteSession.setDisable(true);
        if (course == null || type == null || date == null) {
            markHint.setText("");
            return;
        }
        try {
            recorded = service.getRecordedSessions(course, type);
            markHint.setText(recorded.isEmpty()
                    ? "No sessions recorded yet for this component."
                    : "Recorded sessions: " + recordedText());
            List<AttendanceSheetRow> rows = service.loadSheet(course, type, date);
            sheetRows.setAll(rows);

            boolean saved = rows.stream().anyMatch(AttendanceSheetRow::isAlreadySaved);
            loading = true;
            if (saved) {
                Integer no = sessionNumberOn(date);
                if (no != null) {
                    markSession.setValue(no);
                }
                editBanner.setText("Editing saved session " + markSession.getValue() + " of "
                        + date.format(DATE_FMT) + ". Saving will overwrite the saved records.");
                show(editBanner, true);
            } else if (suggestNumber || markSession.getValue() == null || recorded.containsKey(markSession.getValue())) {
                markSession.setValue(service.suggestNextSessionNo(course, type));
            }
            loading = false;
            btnDeleteSession.setDisable(!saved);
            updateCounts();
        } catch (ServiceException e) {
            loading = false;
            ToUi.message(markMessage, e.getMessage(), false);
        }
    }

    private String recordedText() {
        StringBuilder sb = new StringBuilder();
        for (int no : recorded.keySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(no);
        }
        return sb.toString();
    }

    private Integer sessionNumberOn(LocalDate date) {
        for (Map.Entry<Integer, LocalDate> e : recorded.entrySet()) {
            if (e.getValue().equals(date)) {
                return e.getKey();
            }
        }
        return null;
    }

    private void updateCounts() {
        long present = sheetRows.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
        sheetCounts.setText(sheetRows.isEmpty() ? ""
                : "Present " + present + "  |  Absent " + (sheetRows.size() - present) + "  |  Total " + sheetRows.size());
    }

    @FXML
    private void markAllPresent() {
        setAll(AttendanceStatus.PRESENT);
    }

    @FXML
    private void markAllAbsent() {
        setAll(AttendanceStatus.ABSENT);
    }

    private void setAll(AttendanceStatus status) {
        sheetRows.forEach(r -> r.setStatus(status));
        sheetTable.refresh();
        updateCounts();
    }

    @FXML
    private void saveSheet() {
        TechnicalOfficer to = ToUi.currentOfficer();
        if (to == null) {
            ToUi.message(markMessage, "Please log in as a Technical Officer.", false);
            return;
        }
        Course course = markCourse.getValue();
        SessionType type = markType.getValue();
        Integer no = markSession.getValue();
        LocalDate date = markDate.getValue();
        if (no == null) {
            ToUi.message(markMessage, "Please select the session number.", false);
            return;
        }
        try {
            service.saveSheet(to.getUserId(), course, type, no, date, new ArrayList<>(sheetRows));
            ToUi.message(markMessage, "Attendance saved for " + sheetRows.size() + " undergraduates ("
                    + course.getCourseCode() + ", " + type.getLabel() + " session " + no + ", "
                    + date.format(DATE_FMT) + ").", true);
            onSessionTargetChanged(false);
            ToUi.message(markMessage, "Attendance saved for " + sheetRows.size() + " undergraduates.", true);
        } catch (ValidationException | ServiceException e) {
            ToUi.message(markMessage, e.getMessage(), false);
        }
    }

    @FXML
    private void deleteSession() {
        Course course = markCourse.getValue();
        SessionType type = markType.getValue();
        LocalDate date = markDate.getValue();
        if (!ToUi.confirm("Delete every attendance record of this session (" + date.format(DATE_FMT) + ")?")) {
            return;
        }
        try {
            int n = service.deleteSession(course, type, date);
            onSessionTargetChanged(true);
            ToUi.message(markMessage, n + " attendance records deleted.", true);
        } catch (ValidationException | ServiceException e) {
            ToUi.message(markMessage, e.getMessage(), false);
        }
    }

    // ------------------------------------------------------------------ BATCH SUMMARY

    private void setupBatchTable() {
        batchTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        batchTable.setPlaceholder(new Label("Select a course to see the batch summary."));

        TableColumn<AttendanceSummary, String> reg = ToUi.column("Reg No", 110, AttendanceSummary::getRegNumber);
        TableColumn<AttendanceSummary, String> name = ToUi.column("Undergraduate", 170, AttendanceSummary::getStudentName);
        TableColumn<AttendanceSummary, String> type = ToUi.column("Type", 100, s -> ToUi.studentStatus(s.getStudentStatus()));
        TableColumn<AttendanceSummary, String> attended = ToUi.column("Attended", 80,
                s -> s.getPresentSessions() + " / " + s.getTotalSessions());
        TableColumn<AttendanceSummary, String> raw = ToUi.column("Attendance", 90,
                s -> ToUi.percent(s.getRawPercentage(), s.getTotalSessions() > 0));
        TableColumn<AttendanceSummary, String> med = ToUi.column("Medical absences", 100,
                s -> String.valueOf(s.getMedicalSessions()));
        TableColumn<AttendanceSummary, String> adj = ToUi.column("After medicals", 100,
                s -> ToUi.percent(s.getAdjustedPercentage(), s.getTotalSessions() > 0));
        TableColumn<AttendanceSummary, String> scenario = ToUi.column("Scenario", 190,
                s -> s.getTotalSessions() > 0 ? s.getScenario().getLabel() : "-");
        TableColumn<AttendanceSummary, String> eligible = ToUi.column("Eligible", 80,
                s -> s.getTotalSessions() == 0 ? "-" : (s.isEligible() ? "Yes" : "No"));
        ToUi.chipColumn(eligible, t -> t.equals("Yes") ? "ok" : "bad");

        batchTable.getColumns().addAll(List.of(reg, name, type, attended, raw, med, adj, scenario, eligible));
    }

    private void onBatchCourseChanged() {
        Course course = batchCourse.getValue();
        loading = true;
        batchType.getItems().clear();
        if (course != null) {
            if (course.hasTheory() && course.hasPractical()) {
                batchType.getItems().add(new Component("Combined (theory + practical)", null));
                batchType.getItems().add(new Component("Theory only", SessionType.THEORY));
                batchType.getItems().add(new Component("Practical only", SessionType.PRACTICAL));
            } else if (course.hasTheory()) {
                batchType.getItems().add(new Component("Theory", null));
            } else {
                batchType.getItems().add(new Component("Practical", null));
            }
            batchType.getSelectionModel().selectFirst();
        }
        loading = false;
        refreshBatch();
    }

    private void refreshBatch() {
        if (loading) {
            return;
        }
        Course course = batchCourse.getValue();
        Component component = batchType.getValue();
        String batch = batchBatch.getValue();
        batchData = new ArrayList<>();
        ToUi.message(batchMessage, null, true);
        if (course != null && component != null) {
            try {
                batchData = service.getBatchSummary(course, component.type(),
                        (batch == null || batch.equals(ALL_BATCHES)) ? null : batch);
            } catch (ValidationException | ServiceException e) {
                ToUi.message(batchMessage, e.getMessage(), false);
            }
        }
        renderBatch();
    }

    /** Applies the "Show" filter and fills the table + tiles. */
    private void renderBatch() {
        String filter = batchScenario.getValue() == null ? ALL_STUDENTS : batchScenario.getValue();
        List<AttendanceSummary> shown = new ArrayList<>();
        for (AttendanceSummary s : batchData) {
            boolean ok = switch (filter) {
                case ALL_STUDENTS -> true;
                case ELIGIBLE -> s.isEligible();
                case NOT_ELIGIBLE -> !s.isEligible();
                default -> s.getTotalSessions() > 0 && s.getScenario().getLabel().equals(filter);
            };
            if (ok) {
                shown.add(s);
            }
        }
        batchTable.setItems(FXCollections.observableArrayList(shown));

        long eligible = shown.stream().filter(AttendanceSummary::isEligible).count();
        tileTotal.setText(String.valueOf(shown.size()));
        tileEligible.setText(String.valueOf(eligible));
        tileNotEligible.setText(String.valueOf(shown.size() - eligible));
        double avg = shown.stream().filter(s -> s.getTotalSessions() > 0)
                .mapToDouble(AttendanceSummary::getRawPercentage).average().orElse(Double.NaN);
        tileAverage.setText(Double.isNaN(avg) ? "-" : String.format("%.1f%%", avg));
    }

    // ------------------------------------------------------------------ INDIVIDUAL SUMMARY

    private void setupIndividualTables() {
        indTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        indTable.setPlaceholder(new Label("Select an undergraduate."));
        TableColumn<StudentCourseAttendance, String> course = ToUi.column("Course", 110, s -> s.getCourse().getCourseCode());
        TableColumn<StudentCourseAttendance, String> theory = ToUi.column("Theory", 70, s -> pct(s.getTheory()));
        TableColumn<StudentCourseAttendance, String> practical = ToUi.column("Practical", 70, s -> pct(s.getPractical()));
        TableColumn<StudentCourseAttendance, String> combined = ToUi.column("Combined", 80,
                s -> ToUi.percent(s.getCombined().getRawPercentage(), s.getCombined().getTotalSessions() > 0));
        TableColumn<StudentCourseAttendance, String> adj = ToUi.column("After medicals", 100,
                s -> ToUi.percent(s.getCombined().getAdjustedPercentage(), s.getCombined().getTotalSessions() > 0));
        TableColumn<StudentCourseAttendance, String> eligible = ToUi.column("Eligible", 80,
                s -> s.getCombined().getTotalSessions() == 0 ? "-" : (s.getCombined().isEligible() ? "Yes" : "No"));
        ToUi.chipColumn(eligible, t -> t.equals("Yes") ? "ok" : "bad");
        indTable.getColumns().addAll(List.of(course, theory, practical, combined, adj, eligible));
        indTable.getSelectionModel().selectedItemProperty().addListener((o, a, row) -> refreshLog(row));

        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        logTable.setPlaceholder(new Label("No sessions to show."));
        TableColumn<Attendance, String> type = ToUi.column("Component", 90, a -> a.getSessionType().getLabel());
        TableColumn<Attendance, String> no = ToUi.column("Session", 70, a -> String.valueOf(a.getSessionNo()));
        TableColumn<Attendance, String> date = ToUi.column("Date", 110, a -> a.getSessionDate().format(DATE_FMT));
        TableColumn<Attendance, String> st = ToUi.column("Status", 90, a -> a.getStatus().getLabel());
        ToUi.chipColumn(st, t -> t.equals("Present") ? "ok" : "bad");
        TableColumn<Attendance, String> med = ToUi.column("Medical", 110,
                a -> a.getMedicalStatus() == null ? "-" : a.getMedicalStatus().getLabel());
        ToUi.chipColumn(med, ToUi::approvalKind);
        logTable.getColumns().addAll(List.of(type, no, date, st, med));
    }

    private static String pct(AttendanceSummary s) {
        return s == null || s.getTotalSessions() == 0 ? "-" : ToUi.percent(s.getRawPercentage(), true);
    }

    private void refreshIndividual() {
        Undergraduate student = indStudent.getValue();
        ToUi.message(indMessage, null, true);
        logTable.getItems().clear();
        logHint.setText("Select a course above to see every session.");
        if (student == null) {
            indTable.getItems().clear();
            indSummaryLabel.setText("");
            return;
        }
        try {
            List<StudentCourseAttendance> rows = service.getStudentSummary(student);
            indTable.setItems(FXCollections.observableArrayList(rows));
            long ok = rows.stream().filter(r -> r.getCombined().isEligible()).count();
            indSummaryLabel.setText(student.getFullName() + " (" + student.getRegNumber() + ", "
                    + ToUi.studentStatus(student.getStatus()) + ") is attendance-eligible in " + ok
                    + " of " + rows.size() + " courses.");
        } catch (ValidationException | ServiceException e) {
            ToUi.message(indMessage, e.getMessage(), false);
        }
    }

    private void refreshLog(StudentCourseAttendance row) {
        Undergraduate student = indStudent.getValue();
        if (row == null || student == null) {
            logTable.getItems().clear();
            return;
        }
        try {
            logTitle.setText("Session Log - " + row.getCourse().getCourseCode());
            logHint.setText(row.getCourse().getCourseName());
            logTable.setItems(FXCollections.observableArrayList(service.getSessionLog(student, row.getCourse())));
        } catch (ServiceException e) {
            ToUi.message(indMessage, e.getMessage(), false);
        }
    }
}
