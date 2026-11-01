package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminNotice;
import com.faculty.service.admin.AdminNoticeService;
import com.faculty.util.admin.AdminUi;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Notice Management (admin section "notices"; the header bell opens it too). UI only. */
public class NoticesSectionController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML private TableView<AdminNotice> table;
    @FXML private Label countLabel;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> audienceBox;
    @FXML private TextArea contentArea;

    private final AdminNoticeService noticeService = new AdminNoticeService();

    private int selectedId = 0;

    @FXML
    private void initialize() {
        AdminUi.prepare(table);
        AdminUi.column(table, "ID", n -> String.valueOf(n.noticeId()));
        AdminUi.column(table, "Title", AdminNotice::title);
        AdminUi.column(table, "Audience", AdminNotice::audience);
        AdminUi.column(table, "Posted By", AdminNotice::postedBy);
        AdminUi.column(table, "Posted At", n -> n.postedAt() == null ? "" : n.postedAt().format(DATE_FMT));

        audienceBox.getItems().setAll(AdminNoticeService.AUDIENCES);
        table.getSelectionModel().selectedItemProperty().addListener((o, old, n) -> {
            if (n != null) {
                selectedId = n.noticeId();
                titleField.setText(n.title());
                audienceBox.setValue(n.audience());
                contentArea.setText(n.content());
            }
        });
        clearForm();
        refresh();
    }

    private void refresh() {
        try {
            List<AdminNotice> rows = noticeService.findAll();
            AdminUi.show(table, rows);
            countLabel.setText(rows.size() + " notice(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void clearForm() {
        selectedId = 0;
        table.getSelectionModel().clearSelection();
        titleField.clear();
        contentArea.clear();
        audienceBox.setValue("ALL");
    }

    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleAdd() {
        try {
            noticeService.add(AdminUi.text(titleField), audienceBox.getValue(), AdminUi.text(contentArea));
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            noticeService.update(selectedId, AdminUi.text(titleField), audienceBox.getValue(), AdminUi.text(contentArea));
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedId == 0) {
            AdminUi.warn("Select a notice from the table first.");
            return;
        }
        if (!AdminUi.confirm("Delete this notice?")) {
            return;
        }
        try {
            noticeService.delete(selectedId);
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
