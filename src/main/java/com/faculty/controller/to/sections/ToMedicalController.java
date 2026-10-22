package com.faculty.controller.to.sections;

import com.faculty.MainApp;
import com.faculty.controller.to.ToUi;
import com.faculty.exception.ServiceException;
import com.faculty.exception.ValidationException;
import com.faculty.model.ApprovalStatus;
import com.faculty.model.MedicalRecord;
import com.faculty.model.student.Undergraduate;
import com.faculty.model.to.TechnicalOfficer;
import com.faculty.service.MedicalService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Medical Management screen (FR-36, FR-37, FR-38, BR-05). */
public class ToMedicalController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    private static final String ALL = "All statuses";

    @FXML private Label formTitle;
    @FXML private ComboBox<Undergraduate> medStudent;
    @FXML private DatePicker medDate;
    @FXML private TextField medReason;
    @FXML private Label docLabel;
    @FXML private Label formMessage;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;

    @FXML private TextField listSearch;
    @FXML private ComboBox<String> listStatus;
    @FXML private TableView<MedicalRecord> medTable;
    @FXML private Label listMessage;
    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnOpen;

    private final MedicalService service = new MedicalService();
    private MedicalRecord editing;          // null = adding a new medical
    private File chosenDocument;

    @FXML
    private void initialize() {
        medDate.setValue(LocalDate.now());
        medDate.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isAfter(LocalDate.now()));
            }
        });

        medStudent.setConverter(new StringConverter<>() {
            @Override public String toString(Undergraduate u) {
                return u == null ? "" : u.getRegNumber() + " - " + u.getFullName();
            }
            @Override public Undergraduate fromString(String s) { return null; }
        });
        try {
            medStudent.setItems(FXCollections.observableArrayList(service.getStudents()));
        } catch (ServiceException e) {
            ToUi.message(formMessage, e.getMessage(), false);
        }

        listStatus.getItems().addAll(ALL, ApprovalStatus.PENDING.getLabel(),
                ApprovalStatus.APPROVED.getLabel(), ApprovalStatus.REJECTED.getLabel());
        listStatus.getSelectionModel().selectFirst();
        listStatus.valueProperty().addListener((o, a, b) -> reload());
        listSearch.textProperty().addListener((o, a, b) -> reload());

        setupTable();
        medTable.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> updateButtons());
        reload();
    }

    private void setupTable() {
        medTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        medTable.setPlaceholder(new Label("No medical records found."));
        TableColumn<MedicalRecord, String> reg = ToUi.column("Reg No", 105, MedicalRecord::getRegNumber);
        TableColumn<MedicalRecord, String> name = ToUi.column("Undergraduate", 150, MedicalRecord::getStudentName);
        TableColumn<MedicalRecord, String> date = ToUi.column("Date", 95, m -> m.getSessionDate().format(DATE_FMT));
        TableColumn<MedicalRecord, String> reason = ToUi.column("Reason", 150, m -> m.getReason() == null ? "-" : m.getReason());
        TableColumn<MedicalRecord, String> doc = ToUi.column("Document", 110, m -> fileName(m.getDocumentPath()));
        TableColumn<MedicalRecord, String> status = ToUi.column("Status", 105, m -> m.getApprovalStatus().getLabel());
        ToUi.chipColumn(status, ToUi::approvalKind);
        TableColumn<MedicalRecord, String> by = ToUi.column("Decided by", 110,
                m -> m.getApprovedByName() == null ? "-" : m.getApprovedByName());
        medTable.getColumns().addAll(List.of(reg, name, date, reason, doc, status, by));
    }

    private static String fileName(String path) {
        if (path == null || path.isBlank()) {
            return "-";
        }
        return path.substring(path.lastIndexOf('/') + 1);
    }

    @FXML
    private void reload() {
        ApprovalStatus status = null;
        String selected = listStatus.getValue();
        for (ApprovalStatus s : ApprovalStatus.values()) {
            if (s.getLabel().equals(selected)) {
                status = s;
            }
        }
        try {
            medTable.setItems(FXCollections.observableArrayList(service.find(status, listSearch.getText())));
            ToUi.message(listMessage, null, true);
        } catch (ServiceException e) {
            ToUi.message(listMessage, e.getMessage(), false);
        }
        updateButtons();
    }

    private void updateButtons() {
        MedicalRecord m = medTable.getSelectionModel().getSelectedItem();
        boolean any = m != null;
        btnApprove.setDisable(!any || m.getApprovalStatus() == ApprovalStatus.APPROVED);
        btnReject.setDisable(!any || m.getApprovalStatus() == ApprovalStatus.REJECTED);
        btnEdit.setDisable(!any || !m.isEditable());
        btnDelete.setDisable(!any || !m.isEditable());
        btnOpen.setDisable(!any || m.getDocumentPath() == null);
    }

    // ------------------------------------------------------------------ add / edit form

    @FXML
    private void chooseDocument() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select medical document");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF or image", "*.pdf", "*.jpg", "*.jpeg", "*.png"));
        File file = chooser.showOpenDialog(MainApp.getPrimaryStage());
        if (file != null) {
            chosenDocument = file;
            docLabel.setText(file.getName());
        }
    }

    @FXML
    private void clearDocument() {
        chosenDocument = null;
        docLabel.setText(editing != null && editing.getDocumentPath() != null
                ? "Current: " + fileName(editing.getDocumentPath())
                : "No document chosen (PDF / JPG / PNG, max 5 MB)");
    }

    @FXML
    private void saveMedical() {
        try {
            if (editing == null) {
                service.add(medStudent.getValue(), medDate.getValue(), medReason.getText(), chosenDocument);
                ToUi.message(formMessage, "Medical record added (status: Pending).", true);
            } else {
                service.update(editing, medDate.getValue(), medReason.getText(), chosenDocument);
                ToUi.message(formMessage, "Medical record updated.", true);
            }
            String done = formMessage.getText();
            resetForm();
            ToUi.message(formMessage, done, true);
            reload();
        } catch (ValidationException | ServiceException e) {
            ToUi.message(formMessage, e.getMessage(), false);
        }
    }

    @FXML
    private void cancelEdit() {
        resetForm();
    }

    private void resetForm() {
        editing = null;
        chosenDocument = null;
        formTitle.setText("Add Medical Record");
        btnSave.setText("Save Medical");
        medStudent.setDisable(false);
        medStudent.setValue(null);
        medDate.setValue(LocalDate.now());
        medReason.clear();
        docLabel.setText("No document chosen (PDF / JPG / PNG, max 5 MB)");
        ToUi.message(formMessage, null, true);
    }

    @FXML
    private void editSelected() {
        MedicalRecord m = medTable.getSelectionModel().getSelectedItem();
        if (m == null || !m.isEditable()) {
            return;
        }
        editing = m;
        chosenDocument = null;
        formTitle.setText("Edit Medical Record");
        btnSave.setText("Update Medical");
        medStudent.getItems().stream().filter(u -> u.getUserId() == m.getStuId()).findFirst()
                .ifPresent(medStudent::setValue);
        medStudent.setDisable(true);             // the undergraduate of a record never changes
        medDate.setValue(m.getSessionDate());
        medReason.setText(m.getReason() == null ? "" : m.getReason());
        clearDocument();
        ToUi.message(formMessage, null, true);
    }

    // ------------------------------------------------------------------ decisions & delete

    @FXML
    private void approve() {
        decide(ApprovalStatus.APPROVED);
    }

    @FXML
    private void reject() {
        decide(ApprovalStatus.REJECTED);
    }

    private void decide(ApprovalStatus decision) {
        MedicalRecord m = medTable.getSelectionModel().getSelectedItem();
        TechnicalOfficer to = ToUi.currentOfficer();
        if (m == null || to == null) {
            return;
        }
        try {
            service.decide(m, decision, to.getUserId());
            reload();
            ToUi.message(listMessage, "Medical marked as " + decision.getLabel() + ".", true);
        } catch (ValidationException | ServiceException e) {
            ToUi.message(listMessage, e.getMessage(), false);
        }
    }

    @FXML
    private void deleteSelected() {
        MedicalRecord m = medTable.getSelectionModel().getSelectedItem();
        if (m == null || !ToUi.confirm("Delete the pending medical of " + m.getStudentName() + "?")) {
            return;
        }
        try {
            service.delete(m);
            if (editing != null && editing.getMedicalId() == m.getMedicalId()) {
                resetForm();
            }
            reload();
            ToUi.message(listMessage, "Medical record deleted.", true);
        } catch (ValidationException | ServiceException e) {
            ToUi.message(listMessage, e.getMessage(), false);
        }
    }

    @FXML
    private void openDocument() {
        MedicalRecord m = medTable.getSelectionModel().getSelectedItem();
        if (m == null || m.getDocumentPath() == null) {
            return;
        }
        File file = new File(m.getDocumentPath());
        if (!file.exists()) {
            ToUi.message(listMessage, "The document file was not found on this computer.", false);
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            ToUi.message(listMessage, "Could not open the document.", false);
        }
    }
}
