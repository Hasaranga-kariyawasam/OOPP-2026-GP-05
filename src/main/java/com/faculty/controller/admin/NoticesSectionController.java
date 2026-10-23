package com.faculty.controller.admin;

import com.faculty.model.User;
import com.faculty.service.SessionManager;
import com.faculty.util.admin.AdminDb;
import com.faculty.util.admin.AdminUi;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Notice Management (admin section "notices"; the header bell opens it too). */
public class NoticesSectionController {

    private static final String[] HEADERS = {"ID", "Title", "Audience", "Posted By", "Posted At"};
    // hidden: 5 content

    @FXML private TableView<ObservableList<String>> table;
    @FXML private Label countLabel;
    @FXML private TextField titleField;
    @FXML private ComboBox<String> audienceBox;
    @FXML private TextArea contentArea;

    private int selectedId = 0;

    @FXML
    private void initialize() {
        audienceBox.getItems().setAll("ALL", "UNDERGRADUATE", "LECTURER", "TECHNICAL_OFFICER", "ADMIN");
        table.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) {
                selectedId = Integer.parseInt(r.get(0));
                titleField.setText(r.get(1));
                audienceBox.setValue(r.get(2));
                contentArea.setText(r.get(5));
            }
        });
        clearForm();
        refresh();
    }

    private void refresh() {
        try {
            List<List<String>> rows = AdminDb.query(
                    "SELECT n.notice_id, n.title, n.target_role, COALESCE(CONCAT(u.first_name,' ',u.last_name),'-'), "
                  + "n.posted_date, n.content FROM notice n LEFT JOIN users u ON u.user_id = n.posted_by "
                  + "ORDER BY n.posted_date DESC, n.notice_id DESC");
            AdminUi.fillTable(table, HEADERS, rows);
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

    private boolean valid() {
        if (AdminUi.text(titleField).isEmpty()) { AdminUi.warn("Title is required."); return false; }
        if (AdminUi.text(contentArea).isEmpty()) { AdminUi.warn("Write the notice message."); return false; }
        return true;
    }

    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleAdd() {
        if (!valid()) return;
        User admin = SessionManager.getCurrentUser();
        Integer adminId = admin == null ? null : admin.getUserId();
        try {
            AdminDb.update("INSERT INTO notice (title, content, target_role, posted_by) VALUES (?,?,?,?)",
                    AdminUi.text(titleField), AdminUi.text(contentArea), audienceBox.getValue(), adminId);
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedId == 0) { AdminUi.warn("Select a notice from the table first."); return; }
        if (!valid()) return;
        try {
            AdminDb.update("UPDATE notice SET title=?, content=?, target_role=? WHERE notice_id=?",
                    AdminUi.text(titleField), AdminUi.text(contentArea), audienceBox.getValue(), selectedId);
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedId == 0) { AdminUi.warn("Select a notice from the table first."); return; }
        if (!AdminUi.confirm("Delete this notice?")) return;
        try {
            AdminDb.update("DELETE FROM notice WHERE notice_id=?", selectedId);
            clearForm();
            refresh();
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
