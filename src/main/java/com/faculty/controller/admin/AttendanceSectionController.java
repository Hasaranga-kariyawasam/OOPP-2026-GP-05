package com.faculty.controller.admin;

import com.faculty.util.admin.AdminDb;
import com.faculty.util.admin.AdminUi;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Attendance Records: search by student or course, correct Present / Absent (admin section "attendance"). */
public class AttendanceSectionController {

    private static final String[] HEADERS = {"Reg. Number", "Course", "Type", "Session Date", "Session No.", "Status"};
    // hidden: 6 attendance_id

    @FXML private TableView<ObservableList<String>> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;

    @FXML
    private void initialize() {
        refresh();
    }

    private void refresh() {
        String like = "%" + AdminUi.text(searchField) + "%";
        try {
            List<List<String>> rows = AdminDb.query(
                    "SELECT ug.reg_number, c.course_code, a.session_type, a.session_date, a.session_id, a.status, a.attendance_id "
                  + "FROM attendance a JOIN undergraduate ug ON ug.stu_id = a.stu_id "
                  + "JOIN course c ON c.course_id = a.course_id "
                  + "WHERE ug.reg_number LIKE ? OR c.course_code LIKE ? "
                  + "ORDER BY a.session_date DESC LIMIT 500", like, like);
            AdminUi.fillTable(table, HEADERS, rows);
            countLabel.setText("Found " + rows.size() + " record(s)" + (rows.size() == 500 ? " (showing first 500)." : "."));
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML private void handleSearch() { refresh(); }

    @FXML private void handlePresent() { mark("PRESENT"); }

    @FXML private void handleAbsent() { mark("ABSENT"); }

    private void mark(String status) {
        ObservableList<String> row = table.getSelectionModel().getSelectedItem();
        if (row == null) { AdminUi.warn("Select an attendance record first."); return; }
        try {
            AdminDb.update("UPDATE attendance SET status=? WHERE attendance_id=?", status, Integer.parseInt(row.get(6)));
            int keep = table.getSelectionModel().getSelectedIndex();
            refresh();
            table.getSelectionModel().select(keep);
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
