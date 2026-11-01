package com.faculty.controller.lecture;

import com.faculty.model.lecture.GpaSummaryRow;
import com.faculty.model.lecture.GradeLine;
import com.faculty.service.LecturerGradeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/** "GPA" tab of the Marks screen: SGPA and CGPA of every student; click a student to see the subjects and grades. */
public class LecturerGpaController {

    @FXML private TextField searchField;
    @FXML private Label summaryLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<GpaSummaryRow> table;
    @FXML private TableColumn<GpaSummaryRow, String> colReg;
    @FXML private TableColumn<GpaSummaryRow, String> colName;
    @FXML private TableColumn<GpaSummaryRow, String> colStatus;
    @FXML private TableColumn<GpaSummaryRow, String> colCredits;
    @FXML private TableColumn<GpaSummaryRow, String> colSgpa;
    @FXML private TableColumn<GpaSummaryRow, String> colCgpa;

    // detail panel: subjects and grades of the selected student
    @FXML private Label detailTitle;
    @FXML private Label detailGpa;
    @FXML private TableView<GradeLine> detailTable;
    @FXML private TableColumn<GradeLine, String> dCode;
    @FXML private TableColumn<GradeLine, String> dName;
    @FXML private TableColumn<GradeLine, String> dSemester;
    @FXML private TableColumn<GradeLine, String> dCredits;
    @FXML private TableColumn<GradeLine, String> dMark;
    @FXML private TableColumn<GradeLine, String> dGrade;
    @FXML private TableColumn<GradeLine, String> dPoint;

    private final LecturerGradeService gradeService = new LecturerGradeService();
    private final ObservableList<GpaSummaryRow> data = FXCollections.observableArrayList();
    private FilteredList<GpaSummaryRow> filtered;

    @FXML
    private void initialize() {
        colReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegNumber()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus().replace('_', ' ')));
        colCredits.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTotalCredits())));
        colSgpa.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getSgpa())));
        colCgpa.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getCgpa())));

        dCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseCode()));
        dName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseName()));
        dSemester.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSemester()));
        dCredits.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCredits())));
        dMark.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getFinalMark())));
        dGrade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGrade()));
        dPoint.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1f", c.getValue().getGradePoint())));

        filtered = new FilteredList<>(data, r -> true);
        SortedList<GpaSummaryRow> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setPlaceholder(new Label("No grades recorded yet."));
        detailTable.setPlaceholder(new Label("Select a student."));

        table.getSelectionModel().selectedItemProperty().addListener((o, a, row) -> showDetail(row));
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        showDetail(null);
        load();
    }

    /** Reloads the GPA list (also used when the tab is opened again). */
    @FXML
    public void load() {
        try {
            data.setAll(gradeService.getGpaSummary());
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        } catch (SQLException | SecurityException e) {      // NFR-04: show a message, never crash
            System.err.println("Loading GPA failed: " + e.getMessage());
            messageLabel.setText("Could not load GPA. Please try again later.");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
        applyFilter();
    }

    private void applyFilter() {
        String text = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filtered.setPredicate(r -> text.isEmpty()
                || r.getRegNumber().toLowerCase().contains(text)
                || r.getStudentName().toLowerCase().contains(text));

        double batchCgpa = data.stream().mapToDouble(GpaSummaryRow::getCgpa).average().orElse(0);
        summaryLabel.setText(data.isEmpty() ? "" : String.format("Batch average CGPA %.2f  |  showing %d of %d students",
                batchCgpa, filtered.size(), data.size()));
    }

    private void showDetail(GpaSummaryRow row) {
        if (row == null) {
            detailTitle.setText("Subjects and grades");
            detailGpa.setText("Click a student in the list.");
            detailTable.getItems().clear();
            return;
        }
        List<GradeLine> lines = row.getLines();
        detailTitle.setText(row.getRegNumber() + " - " + row.getStudentName());

        StringBuilder gpa = new StringBuilder();
        for (String sem : new TreeSet<>(lines.stream().map(GradeLine::getSemester).toList())) {
            gpa.append("SGPA ").append(sem).append(": ")
               .append(String.format("%.2f", gradeService.semesterGpa(lines, sem))).append("   ");
        }
        gpa.append("CGPA: ").append(String.format("%.2f", row.getCgpa()));
        detailGpa.setText(gpa.toString());

        detailTable.setItems(FXCollections.observableArrayList(
                lines.stream().sorted(Comparator.comparing(GradeLine::getSemester).thenComparing(GradeLine::getCourseCode)).toList()));
    }
}
