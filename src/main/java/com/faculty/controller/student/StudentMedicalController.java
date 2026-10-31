package com.faculty.controller.student;

import com.faculty.model.student.StudentRecords.MedicalInfo;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Medical records of the logged-in student.
 * The student can submit a medical (date, reason, certificate); a technical officer approves it later.
 */
public class StudentMedicalController {

    private static final Path MEDICAL_DIR = Path.of("uploads", "medicals");

    @FXML private DatePicker datePicker;
    @FXML private TextField reasonField;
    @FXML private Label fileLabel;
    @FXML private Label summaryLabel;
    @FXML private TableView<MedicalInfo> medicalTable;

    private final StudentService service = new StudentService();
    private File chosenFile;

    @FXML
    private void initialize() {
        StudentUi.addColumn(medicalTable, "Medical Date", MedicalInfo::date);
        StudentUi.addColumn(medicalTable, "Reason", MedicalInfo::reason);
        StudentUi.addStatusColumn(medicalTable, "Status", MedicalInfo::status);
        StudentUi.addColumn(medicalTable, "Document", m -> m.document().isEmpty() ? "-" : new File(m.document()).getName());
        StudentUi.fitColumns(medicalTable);

        // a medical can only be for today or an earlier day
        datePicker.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isAfter(LocalDate.now()));
            }
        });
        datePicker.setEditable(false);

        loadMedicals();
    }

    private void loadMedicals() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        try {
            List<MedicalInfo> list = service.dao().findMedicals(s.getUserId());
            medicalTable.setItems(FXCollections.observableArrayList(list));
            long approved = list.stream().filter(m -> "APPROVED".equals(m.status())).count();
            long pending = list.stream().filter(m -> "PENDING".equals(m.status())).count();
            long rejected = list.stream().filter(m -> "REJECTED".equals(m.status())).count();
            summaryLabel.setText(list.size() + " medical(s)  |  Approved: " + approved
                    + "  |  Pending: " + pending + "  |  Rejected: " + rejected);
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }

    // ------------------------------------------------------------------ actions

    @FXML
    private void handleChooseDocument() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose medical certificate");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF or image", "*.pdf", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(datePicker.getScene().getWindow());
        if (file != null) {
            chosenFile = file;
            fileLabel.setText(file.getName());
        }
    }

    @FXML
    private void handleSubmit() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        LocalDate date = datePicker.getValue();
        String reason = StudentUi.text(reasonField.getText());

        if (date == null) {
            StudentUi.warn("Please pick the medical date.");
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            StudentUi.warn("The medical date cannot be in the future.");
            return;
        }
        if (reason.isEmpty() || reason.length() > 255) {
            StudentUi.warn("Enter the reason (up to 255 characters).");
            return;
        }
        if (chosenFile == null) {
            StudentUi.warn("Please attach your medical certificate.");
            return;
        }

        try {
            String documentPath = copyDocument(s, date, chosenFile);
            int id = service.dao().submitMedical(s.getUserId(), date, reason, documentPath);
            if (id < 0) {
                StudentUi.warn("You already have a pending or approved medical for " + date + ".");
                return;
            }
            StudentUi.info("Medical submitted. It is PENDING until a technical officer approves it.");
            datePicker.setValue(null);
            reasonField.clear();
            chosenFile = null;
            fileLabel.setText("No file chosen");
            loadMedicals();
        } catch (IOException e) {
            StudentUi.warn("Could not save the document file. Please try another file.");
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }

    /** Copies the certificate to uploads/medicals/ as TG_2024_1013_2026-08-05.pdf (same naming as the seed data). */
    private String copyDocument(Undergraduate s, LocalDate date, File source) throws IOException {
        Files.createDirectories(MEDICAL_DIR);
        String name = source.getName();
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')).toLowerCase() : ".pdf";
        String base = StudentUi.text(s.getRegNumber()).replace('/', '_') + "_" + date;
        Path target = MEDICAL_DIR.resolve(base + ext);
        int n = 2;
        while (Files.exists(target)) {          // never overwrite an earlier certificate
            target = MEDICAL_DIR.resolve(base + "_" + n++ + ext);
        }
        Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString().replace('\\', '/');
    }
}
