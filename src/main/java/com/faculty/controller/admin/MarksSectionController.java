package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminMark;
import com.faculty.model.admin.AdminMarkForm;
import com.faculty.model.admin.AdminOption;
import com.faculty.service.admin.AdminLookupService;
import com.faculty.service.admin.AdminMarkService;
import com.faculty.util.admin.AdminUi;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Marks Management: view, add, correct and delete individual marks (admin section "marks"). UI only. */
public class MarksSectionController {

    @FXML private TableView<AdminMark> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TextField regField;
    @FXML private ComboBox<AdminOption> courseBox;
    @FXML private TextField evalField;
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextField valueField;

    private final AdminMarkService markService = new AdminMarkService();
    private final AdminLookupService lookupService = new AdminLookupService();

    private int selectedId = 0;

    @FXML
    private void initialize() {
        AdminUi.prepare(table);
        AdminUi.column(table, "Reg. Number", AdminMark::regNumber);
        AdminUi.column(table, "Course", AdminMark::courseCode);
        AdminUi.column(table, "Evaluation", AdminMark::evaluationType);
        AdminUi.column(table, "Category", AdminMark::category);
        AdminUi.column(table, "Mark (/100)", m -> String.format("%.2f", m.value()));

        categoryBox.getItems().setAll("CA", "FINAL");
        try {
            courseBox.getItems().setAll(lookupService.courses());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
        table.getSelectionModel().selectedItemProperty().addListener((o, old, m) -> {
            if (m != null) {
                selectedId = m.markId();
                regField.setText(m.regNumber());
                AdminUi.select(courseBox, m.courseId());
                evalField.setText(m.evaluationType());
                categoryBox.setValue(m.category());
                valueField.setText(String.valueOf(m.value()));
            }
        });
        clearForm();
        refresh();
    }

    private void refresh() {
        try {
            List<AdminMark> rows = markService.search(AdminUi.text(searchField));
            AdminUi.show(table, rows);
            countLabel.setText(rows.size() + " mark(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void clearForm() {
        selectedId = 0;
        table.getSelectionModel().clearSelection();
        regField.clear();
        evalField.clear();
        valueField.clear();
        categoryBox.setValue("CA");
        courseBox.getSelectionModel().clearSelection();
    }

    private AdminMarkForm readForm() {
        return new AdminMarkForm(AdminUi.text(regField), AdminUi.idOf(courseBox), AdminUi.text(evalField),
                categoryBox.getValue(), AdminUi.text(valueField));
    }

    @FXML private void handleSearch() { refresh(); }

    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleAdd() {
        try {
            markService.add(readForm());
            AdminUi.info("Mark added.");
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
            markService.update(selectedId, readForm());
            AdminUi.info("Mark updated.");
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
            AdminUi.warn("Select a mark from the table first.");
            return;
        }
        if (!AdminUi.confirm("Delete this mark?")) {
            return;
        }
        try {
            markService.delete(selectedId);
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
