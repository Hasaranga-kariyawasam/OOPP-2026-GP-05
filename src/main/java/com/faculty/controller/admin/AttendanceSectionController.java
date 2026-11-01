package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminAttendanceRow;
import com.faculty.service.admin.AdminAttendanceService;
import com.faculty.util.admin.AdminUi;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Attendance Records: search by student or course, correct Present / Absent (admin section "attendance"). UI only. */
public class AttendanceSectionController {

    @FXML private TableView<AdminAttendanceRow> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    private final AdminAttendanceService attendanceService = new AdminAttendanceService();

    @FXML
    private void initialize() {
        AdminUi.prepare(table);
        AdminUi.column(table, "Reg. Number", AdminAttendanceRow::regNumber);
        AdminUi.column(table, "Course", AdminAttendanceRow::courseCode);
        AdminUi.column(table, "Type", AdminAttendanceRow::sessionType);
        AdminUi.column(table, "Session Date", r -> String.valueOf(r.sessionDate()));
        AdminUi.column(table, "Session No.", r -> String.valueOf(r.sessionNo()));
        AdminUi.column(table, "Status", AdminAttendanceRow::status);
        refresh();
    }

    private void refresh() {
        try {
            List<AdminAttendanceRow> rows = attendanceService.search(AdminUi.text(searchField));
            AdminUi.show(table, rows);
            boolean capped = rows.size() == AdminAttendanceService.SEARCH_LIMIT;
            countLabel.setText("Found " + rows.size() + " record(s)"
                    + (capped ? " (showing first " + AdminAttendanceService.SEARCH_LIMIT + ")." : "."));
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML private void handleSearch() { refresh(); }

    @FXML
    private void handlePresent() {
        change(true);
    }

    @FXML
    private void handleAbsent() {
        change(false);
    }

    private void change(boolean present) {
        AdminAttendanceRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) { AdminUi.warn("Select an attendance record first."); return; }
        try {
            if (present) {
                attendanceService.markPresent(row.attendanceId());
            } else {
                attendanceService.markAbsent(row.attendanceId());
            }
            int keep = table.getSelectionModel().getSelectedIndex();
            refresh();
            table.getSelectionModel().select(keep);
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
