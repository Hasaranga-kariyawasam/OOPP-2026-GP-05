package com.faculty.controller.lecture;

import com.faculty.model.lecture.LecturerTimetableEntry;
import com.faculty.service.LecturerTimetableService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** "Timetable" screen: read-only weekly slots of the logged-in lecturer's courses. */
public class LecturerTimetableController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final Map<String, String> DAYS = Map.of(
            "MON", "Monday", "TUE", "Tuesday", "WED", "Wednesday", "THU", "Thursday",
            "FRI", "Friday", "SAT", "Saturday", "SUN", "Sunday");

    @FXML private Label summaryLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<LecturerTimetableEntry> table;
    @FXML private TableColumn<LecturerTimetableEntry, String> colDay;
    @FXML private TableColumn<LecturerTimetableEntry, String> colTime;
    @FXML private TableColumn<LecturerTimetableEntry, String> colCode;
    @FXML private TableColumn<LecturerTimetableEntry, String> colName;
    @FXML private TableColumn<LecturerTimetableEntry, String> colType;
    @FXML private TableColumn<LecturerTimetableEntry, String> colVenue;

    private final LecturerTimetableService service = new LecturerTimetableService();

    @FXML
    private void initialize() {
        colDay.setCellValueFactory(c -> new SimpleStringProperty(DAYS.getOrDefault(c.getValue().getDay(), c.getValue().getDay())));
        colTime.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getStartTime().format(TIME_FMT) + " - " + c.getValue().getEndTime().format(TIME_FMT)));
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseCode()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseName()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(
                "THEORY".equals(c.getValue().getSessionType()) ? "Theory" : "Practical"));
        colVenue.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getVenue() == null ? "-" : c.getValue().getVenue()));

        table.setPlaceholder(new Label("No timetable slots for your courses yet."));
        try {
            List<LecturerTimetableEntry> list = service.getMyTimetable();
            table.setItems(FXCollections.observableArrayList(list));
            summaryLabel.setText(list.size() + (list.size() == 1 ? " session" : " sessions") + " per week");
        } catch (SQLException | SecurityException e) {      // NFR-04: show a message, never crash
            System.err.println("Loading timetable failed: " + e.getMessage());
            messageLabel.setText("Could not load your timetable. Please try again later.");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }
}
