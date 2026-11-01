package com.faculty.controller.lecture;

import com.faculty.model.lecture.MedicalEntry;
import com.faculty.service.LecturerMedicalService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** "Medical Records" screen: view-only medicals of the students in the lecturer's courses. */
public class LecturerMedicalController {

    private static final String ALL_STATUSES = "All statuses";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label countLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<MedicalEntry> table;
    @FXML private TableColumn<MedicalEntry, String> colReg;
    @FXML private TableColumn<MedicalEntry, String> colName;
    @FXML private TableColumn<MedicalEntry, String> colDate;
    @FXML private TableColumn<MedicalEntry, String> colReason;
    @FXML private TableColumn<MedicalEntry, String> colStatus;
    @FXML private TableColumn<MedicalEntry, String> colDecidedBy;

    private final LecturerMedicalService service = new LecturerMedicalService();
    private FilteredList<MedicalEntry> filtered;
    private int totalCount;

    @FXML
    private void initialize() {
        colReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegNumber()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSessionDate() == null ? "-" : c.getValue().getSessionDate().format(DATE_FMT)));
        colReason.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getReason())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getApprovalStatus()));
        colDecidedBy.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getDecidedBy())));

        List<MedicalEntry> all = load();
        totalCount = all.size();
        filtered = new FilteredList<>(FXCollections.observableArrayList(all), m -> true);
        SortedList<MedicalEntry> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setPlaceholder(new Label("No medical records for your students."));

        statusCombo.getItems().addAll(ALL_STATUSES, "PENDING", "APPROVED", "REJECTED");
        statusCombo.setValue(ALL_STATUSES);

        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        statusCombo.valueProperty().addListener((o, a, b) -> applyFilter());
        applyFilter();
    }

    private List<MedicalEntry> load() {
        try {
            return service.getMedicals();
        } catch (SQLException | SecurityException e) {      // NFR-04: show a message, never crash
            System.err.println("Loading medicals failed: " + e.getMessage());
            messageLabel.setText("Could not load medical records. Please try again later.");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
            return List.of();
        }
    }

    private void applyFilter() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String status = statusCombo.getValue();
        filtered.setPredicate(m -> {
            boolean statusOk = status == null || ALL_STATUSES.equals(status) || status.equals(m.getApprovalStatus());
            boolean textOk = text.isEmpty()
                    || m.getRegNumber().toLowerCase().contains(text)
                    || m.getStudentName().toLowerCase().contains(text);
            return statusOk && textOk;
        });
        countLabel.setText("Showing " + filtered.size() + " of " + totalCount + " records");
    }

    private String nz(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
