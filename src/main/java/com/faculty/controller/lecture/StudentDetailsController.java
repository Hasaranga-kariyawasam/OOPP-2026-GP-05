package com.faculty.controller.lecture;

import com.faculty.model.lecture.StudentDetails;
import com.faculty.service.StudentDetailsService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

/** "Student Details" screen: searchable, filterable, view-only list of undergraduates. */
public class StudentDetailsController {

    private static final String ALL_BATCHES = "All batches";
    private static final String ALL_STATUSES = "All statuses";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    @FXML private TextField searchField;
    @FXML private ComboBox<String> batchCombo;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label countLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<StudentDetails> table;
    @FXML private TableColumn<StudentDetails, String> colReg;
    @FXML private TableColumn<StudentDetails, String> colName;
    @FXML private TableColumn<StudentDetails, String> colBatch;
    @FXML private TableColumn<StudentDetails, String> colStatus;
    @FXML private TableColumn<StudentDetails, String> colDept;

    // detail card
    @FXML private Label dName, dReg, dEmail, dPhone, dNic, dDob, dAddress, dDept, dBatch, dStatus, dIntake, dAccount;

    private final StudentDetailsService service = new StudentDetailsService();
    private FilteredList<StudentDetails> filtered;
    private int totalCount;

    @FXML
    private void initialize() {
        colReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegNumber()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colBatch.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBatch()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colDept.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDepartmentName()));

        List<StudentDetails> all = loadAll();
        totalCount = all.size();

        filtered = new FilteredList<>(FXCollections.observableArrayList(all), s -> true);
        SortedList<StudentDetails> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);

        TreeSet<String> batches = new TreeSet<>();
        for (StudentDetails s : all) {
            if (s.getBatch() != null) batches.add(s.getBatch());
        }
        batchCombo.getItems().add(ALL_BATCHES);
        batchCombo.getItems().addAll(batches);
        batchCombo.setValue(ALL_BATCHES);

        statusCombo.getItems().addAll(ALL_STATUSES, "REGULAR", "REPEAT", "BATCH_MISSED");
        statusCombo.setValue(ALL_STATUSES);

        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        batchCombo.valueProperty().addListener((o, a, b) -> applyFilter());
        statusCombo.valueProperty().addListener((o, a, b) -> applyFilter());
        table.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> showDetails(s));

        showDetails(null);
        updateCount();
    }

    private List<StudentDetails> loadAll() {
        try {
            return service.getAllUndergraduates();
        } catch (SQLException | SecurityException e) {      // NFR-04: show a message, never crash
            System.err.println("Loading undergraduates failed: " + e.getMessage());
            messageLabel.setText("Could not load undergraduate details. Please try again later.");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
            return List.of();
        }
    }

    // ------------------------------------------------------------------ filtering

    private void applyFilter() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String batch = batchCombo.getValue();
        String status = statusCombo.getValue();

        filtered.setPredicate(s -> {
            boolean batchOk = batch == null || ALL_BATCHES.equals(batch) || batch.equals(s.getBatch());
            boolean statusOk = status == null || ALL_STATUSES.equals(status) || status.equals(s.getStatus());
            boolean textOk = text.isEmpty()
                    || contains(s.getRegNumber(), text)
                    || contains(s.getFullName(), text)
                    || contains(s.getEmail(), text);
            return batchOk && statusOk && textOk;
        });
        updateCount();
    }

    private boolean contains(String value, String lowerText) {
        return value != null && value.toLowerCase().contains(lowerText);
    }

    private void updateCount() {
        countLabel.setText("Showing " + filtered.size() + " of " + totalCount + " undergraduates");
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        batchCombo.setValue(ALL_BATCHES);
        statusCombo.setValue(ALL_STATUSES);
        table.getSelectionModel().clearSelection();
    }

    // ------------------------------------------------------------------ detail card

    private void showDetails(StudentDetails s) {
        if (s == null) {
            dName.setText("Select an undergraduate from the list");
            for (Label l : new Label[]{dReg, dEmail, dPhone, dNic, dDob, dAddress, dDept, dBatch, dStatus, dIntake, dAccount}) {
                l.setText("-");
            }
            return;
        }
        dName.setText(s.getFullName());
        dReg.setText(text(s.getRegNumber()));
        dEmail.setText(text(s.getEmail()));
        dPhone.setText(text(s.getPhone()));
        dNic.setText(text(s.getNic()));
        dDob.setText(date(s.getDob()));
        dAddress.setText(text(s.getAddress()));
        dDept.setText(text(s.getDepartmentName()));
        dBatch.setText(text(s.getBatch()));
        dStatus.setText(text(s.getStatus()));
        dIntake.setText(date(s.getIntakeDate()));
        dAccount.setText(s.isActive() ? "Active" : "Deactivated");
    }

    private String text(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String date(LocalDate d) {
        return d == null ? "-" : d.format(DATE_FMT);
    }
}
