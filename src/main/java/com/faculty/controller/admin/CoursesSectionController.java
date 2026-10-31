package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.admin.AdminCourse;
import com.faculty.model.admin.AdminCourseForm;
import com.faculty.model.admin.AdminOption;
import com.faculty.service.admin.AdminCourseService;
import com.faculty.service.admin.AdminLookupService;
import com.faculty.util.admin.AdminUi;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;

/** Course Management (admin section "courses"). UI only. */
public class CoursesSectionController {

    private static final AdminOption NONE = new AdminOption(0, "- No lecturer -");

    @FXML private TableView<AdminCourse> table;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private TextField theoryField;
    @FXML private TextField practicalField;
    @FXML private TextField semesterField;
    @FXML private ComboBox<String> typeBox;
    @FXML private ComboBox<AdminOption> lecturerBox;

    private final AdminCourseService courseService = new AdminCourseService();
    private final AdminLookupService lookupService = new AdminLookupService();

    private int selectedId = 0;

    @FXML
    private void initialize() {
        AdminUi.prepare(table);
        AdminUi.column(table, "Code", AdminCourse::code);
        AdminUi.column(table, "Name", AdminCourse::name);
        AdminUi.column(table, "Theory Cr.", c -> String.valueOf(c.theoryCredits()));
        AdminUi.column(table, "Practical Cr.", c -> String.valueOf(c.practicalCredits()));
        AdminUi.column(table, "Semester", AdminCourse::semester);
        AdminUi.column(table, "Type", AdminCourse::type);
        AdminUi.column(table, "Lecturer", AdminCourse::lecturerName);

        typeBox.getItems().setAll("THEORY", "PRACTICAL", "BOTH");
        try {
            lecturerBox.getItems().add(NONE);
            lecturerBox.getItems().addAll(lookupService.lecturers());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
        table.getSelectionModel().selectedItemProperty().addListener((o, old, r) -> {
            if (r != null) {
                fillForm(r);
            }
        });
        clearForm();
        refresh();
    }

    private void refresh() {
        try {
            List<AdminCourse> rows = courseService.search(AdminUi.text(searchField));
            AdminUi.show(table, rows);
            countLabel.setText(rows.size() + " course(s)");
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }

    private void fillForm(AdminCourse c) {
        selectedId = c.courseId();
        codeField.setText(c.code());
        nameField.setText(c.name());
        theoryField.setText(String.valueOf(c.theoryCredits()));
        practicalField.setText(String.valueOf(c.practicalCredits()));
        semesterField.setText(c.semester());
        typeBox.setValue(c.type());
        if (c.lecturerId() == null) {
            lecturerBox.setValue(NONE);
        } else {
            AdminUi.select(lecturerBox, c.lecturerId());
        }
    }

    private void clearForm() {
        selectedId = 0;
        table.getSelectionModel().clearSelection();
        codeField.clear();
        nameField.clear();
        theoryField.setText("3");
        practicalField.setText("0");
        semesterField.clear();
        typeBox.setValue("THEORY");
        lecturerBox.setValue(NONE);
    }

    private AdminCourseForm readForm() {
        int lecturerId = AdminUi.idOf(lecturerBox);
        return new AdminCourseForm(AdminUi.text(codeField), AdminUi.text(nameField), AdminUi.text(theoryField),
                AdminUi.text(practicalField), AdminUi.text(semesterField), typeBox.getValue(),
                lecturerId == 0 ? null : lecturerId);
    }

    @FXML private void handleSearch() { refresh(); }

    @FXML private void handleClear() { clearForm(); }

    @FXML
    private void handleAdd() {
        try {
            courseService.add(readForm());
            AdminUi.info("Course saved.");
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
            courseService.update(selectedId, readForm());
            AdminUi.info("Course updated.");
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
            AdminUi.warn("Select a course from the table first.");
            return;
        }
        if (!AdminUi.confirm("Delete this course? Its marks, attendance and timetable entries are deleted too.")) {
            return;
        }
        try {
            courseService.delete(selectedId);
            clearForm();
            refresh();
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
