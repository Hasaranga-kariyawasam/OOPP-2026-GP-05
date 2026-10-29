package com.faculty.controller.student;

import com.faculty.model.student.StudentRecords.MedicalInfo;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/** Medical records of the logged-in student (view only - technical officers maintain them). */
public class StudentMedicalController {

    @FXML private Label summaryLabel;
    @FXML private TableView<MedicalInfo> medicalTable;

    private final StudentService service = new StudentService();

    @FXML
    private void initialize() {
        StudentUi.addColumn(medicalTable, "Medical Date", MedicalInfo::date);
        StudentUi.addColumn(medicalTable, "Reason", MedicalInfo::reason);
        StudentUi.addStatusColumn(medicalTable, "Status", MedicalInfo::status);
        StudentUi.addColumn(medicalTable, "Document", m -> m.document().isEmpty() ? "-" : new File(m.document()).getName());
        StudentUi.fitColumns(medicalTable);

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
}
