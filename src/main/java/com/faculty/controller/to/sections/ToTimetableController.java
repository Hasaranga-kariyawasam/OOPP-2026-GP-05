package com.faculty.controller.to.sections;

import com.faculty.controller.to.ToUi;
import com.faculty.exception.ServiceException;
import com.faculty.model.TimetableEntry;
import com.faculty.model.to.TechnicalOfficer;
import com.faculty.service.TimetableService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Department timetable, grouped by weekday (FR-69). */
public class ToTimetableController {

    private static final String ALL = "All courses";

    @FXML private ComboBox<String> courseFilter;
    @FXML private Label ttMessage;
    @FXML private VBox dayBox;

    private final TimetableService service = new TimetableService();
    private List<TimetableEntry> all = new ArrayList<>();

    @FXML
    private void initialize() {
        TechnicalOfficer to = ToUi.currentOfficer();
        if (to == null) {
            return;
        }
        try {
            all = service.getTimetable(to.getDepId());
        } catch (ServiceException e) {
            ToUi.message(ttMessage, e.getMessage(), false);
        }
        List<String> courses = new ArrayList<>();
        courses.add(ALL);
        all.stream().map(t -> t.getCourseCode() + " - " + t.getCourseName()).distinct().sorted().forEach(courses::add);
        courseFilter.setItems(FXCollections.observableArrayList(courses));
        courseFilter.getSelectionModel().selectFirst();
        courseFilter.valueProperty().addListener((o, a, b) -> render());
        render();
    }

    private void render() {
        dayBox.getChildren().clear();
        String filter = courseFilter.getValue();
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        boolean anything = false;

        for (DayOfWeek day : DayOfWeek.values()) {
            List<TimetableEntry> entries = new ArrayList<>();
            for (TimetableEntry t : service.filterByDay(all, day)) {
                if (filter == null || filter.equals(ALL) || filter.equals(t.getCourseCode() + " - " + t.getCourseName())) {
                    entries.add(t);
                }
            }
            if (entries.isEmpty()) {
                continue;
            }
            anything = true;

            Label dayName = new Label(day.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            dayName.getStyleClass().add("to-day-title");
            HBox head = new HBox(10, dayName);
            head.setAlignment(Pos.CENTER_LEFT);
            if (day == today) {
                Label tag = new Label("Today");
                tag.getStyleClass().add("to-day-today");
                head.getChildren().add(tag);
            }

            VBox card = new VBox(10, head);
            card.getStyleClass().add("adm-card");
            for (TimetableEntry t : entries) {
                card.getChildren().add(slot(t));
            }
            dayBox.getChildren().add(card);
        }
        if (!anything) {
            Label none = new Label("No timetable entries found.");
            none.getStyleClass().add("adm-muted");
            dayBox.getChildren().add(none);
        }
    }

    private HBox slot(TimetableEntry t) {
        Label time = new Label(t.getTimeRange());
        time.getStyleClass().add("to-slot-time");
        time.setMinWidth(104);

        Label title = new Label(t.getCourseCode() + " - " + t.getCourseName());
        title.getStyleClass().add("to-slot-title");
        title.setWrapText(true);
        Label meta = new Label((t.getVenue() == null ? "" : t.getVenue()) + "  |  Lecturer: " + t.getLecturerName());
        meta.getStyleClass().add("to-slot-meta");
        meta.setWrapText(true);
        VBox text = new VBox(2, title, meta);
        HBox.setHgrow(text, Priority.ALWAYS);

        Label type = ToUi.chip(t.getSessionType().getLabel(), t.getSessionType().name().equals("THEORY") ? "info" : "neutral");
        HBox row = new HBox(12, time, text, type);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("to-slot");
        return row;
    }
}
