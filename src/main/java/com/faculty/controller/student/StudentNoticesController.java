package com.faculty.controller.student;

import com.faculty.model.student.StudentRecords.NoticeInfo;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.sql.SQLException;
import java.util.List;

/** Notices meant for everyone or for undergraduates (view only). */
public class StudentNoticesController {

    @FXML private Label countLabel;
    @FXML private TableView<NoticeInfo> noticeTable;
    @FXML private Label titleLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea bodyArea;

    private final StudentService service = new StudentService();

    @FXML
    private void initialize() {
        StudentUi.addColumn(noticeTable, "Title", NoticeInfo::title);
        StudentUi.addColumn(noticeTable, "Posted", NoticeInfo::date);
        StudentUi.fitColumns(noticeTable);

        noticeTable.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            if (n != null) {
                titleLabel.setText(n.title());
                dateLabel.setText("Posted on " + n.date());
                bodyArea.setText(n.content());
            }
        });

        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        try {
            List<NoticeInfo> notices = service.dao().findNotices(s.getDepId());
            noticeTable.setItems(FXCollections.observableArrayList(notices));
            countLabel.setText(notices.size() + " notice(s)");
            if (!notices.isEmpty()) {
                noticeTable.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }
}
