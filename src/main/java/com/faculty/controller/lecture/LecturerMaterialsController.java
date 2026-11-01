package com.faculty.controller.lecture;

import com.faculty.model.lecture.LecturerCourse;
import com.faculty.model.lecture.LecturerMaterial;
import com.faculty.service.LecturerCourseService;
import com.faculty.service.LecturerMaterialService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.List;

/**
 * "Course Materials" screen.
 * Pick one of your courses, add a material (title + URL), or click a title in the table to load
 * that material back into the form and change it with "Update".
 */
public class LecturerMaterialsController {

    private static final String OK_STYLE = "-fx-text-fill: #15803d;";
    private static final String ERROR_STYLE = "-fx-text-fill: #b91c1c;";

    @FXML private ComboBox<LecturerCourse> courseCombo;
    @FXML private TextField title;      // field name: title
    @FXML private TextField file;       // field name: file (the URL)
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Label messageLabel;

    @FXML private TableView<LecturerMaterial> table;
    @FXML private TableColumn<LecturerMaterial, String> colTitle;
    @FXML private TableColumn<LecturerMaterial, String> colUrl;

    private final LecturerCourseService courseService = new LecturerCourseService();
    private final LecturerMaterialService materialService = new LecturerMaterialService();

    /** The material currently loaded into the form (null = nothing loaded, so "Update" is off). */
    private LecturerMaterial selectedMaterial;

    @FXML
    private void initialize() {
        courseCombo.setConverter(new StringConverter<>() {
            @Override public String toString(LecturerCourse c) {
                return c == null ? "" : c.getCourseCode() + " - " + c.getCourseName();
            }
            @Override public LecturerCourse fromString(String s) { return null; }
        });

        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colUrl.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFilePath()));
        colTitle.setCellFactory(col -> new ClickableTitleCell());
        table.setPlaceholder(new Label("No materials added for this course yet."));

        updateButton.setDisable(true);
        courseCombo.valueProperty().addListener((o, a, b) -> {
            clearForm();
            hideMessage();
            loadMaterials();
        });

        loadCourses();
    }

    // ------------------------------------------------------------------ loading

    private void loadCourses() {
        try {
            List<LecturerCourse> courses = courseService.getMyCourses();
            courseCombo.setItems(FXCollections.observableArrayList(courses));
            if (courses.isEmpty()) {
                setFormDisabled(true);
                showError("No courses are assigned to you, so you cannot add materials yet.");
            } else {
                courseCombo.getSelectionModel().selectFirst();   // triggers loadMaterials()
            }
        } catch (SQLException | SecurityException e) {
            System.err.println("Loading courses failed: " + e.getMessage());
            setFormDisabled(true);
            showError("Could not load your courses. Please try again later.");
        }
    }

    private void loadMaterials() {
        LecturerCourse course = courseCombo.getValue();
        if (course == null) {
            table.getItems().clear();
            return;
        }
        try {
            table.setItems(FXCollections.observableArrayList(materialService.getMaterials(course.getCourseId())));
        } catch (SQLException | SecurityException e) {
            System.err.println("Loading materials failed: " + e.getMessage());
            table.getItems().clear();
            showError("Could not load the materials. Please try again later.");
        }
    }

    // ------------------------------------------------------------------ buttons

    @FXML
    private void handleAdd() {
        LecturerCourse course = courseCombo.getValue();
        if (course == null) {
            showError("Please select a course first.");
            return;
        }
        try {
            materialService.addMaterial(course.getCourseId(), title.getText(), file.getText());
            clearForm();
            loadMaterials();
            showSuccess("Material added.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException | SecurityException e) {
            System.err.println("Adding material failed: " + e.getMessage());
            showError("The material could not be saved. Please try again later.");
        }
    }

    @FXML
    private void handleUpdate() {
        LecturerCourse course = courseCombo.getValue();
        if (course == null || selectedMaterial == null) {
            showError("Click the title of a material in the table first.");
            return;
        }
        try {
            materialService.updateMaterial(selectedMaterial.getMaterialId(), course.getCourseId(),
                    title.getText(), file.getText());
            clearForm();
            loadMaterials();
            showSuccess("Material updated.");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        } catch (SQLException | SecurityException e) {
            System.err.println("Updating material failed: " + e.getMessage());
            showError("The material could not be updated. Please try again later.");
        }
    }

    // ------------------------------------------------------------------ form helpers

    /** Called when the lecturer clicks a title in the table. */
    private void loadIntoForm(LecturerMaterial material) {
        selectedMaterial = material;
        title.setText(material.getTitle());
        file.setText(material.getFilePath());
        updateButton.setDisable(false);
        hideMessage();
    }

    private void clearForm() {
        selectedMaterial = null;
        title.clear();
        file.clear();
        updateButton.setDisable(true);
        table.getSelectionModel().clearSelection();
    }

    private void setFormDisabled(boolean disabled) {
        courseCombo.setDisable(disabled);
        title.setDisable(disabled);
        file.setDisable(disabled);
        addButton.setDisable(disabled);
        updateButton.setDisable(true);
    }

    private void showError(String text) { show(text, ERROR_STYLE); }
    private void showSuccess(String text) { show(text, OK_STYLE); }

    private void show(String text, String style) {
        messageLabel.setText(text);
        messageLabel.setStyle(style);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private void hideMessage() {
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    // ------------------------------------------------------------------ table cell

    /** Title cell that looks like a link; clicking it loads the material into the form. */
    private class ClickableTitleCell extends TableCell<LecturerMaterial, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("");
                setCursor(Cursor.DEFAULT);
                setOnMouseClicked(null);
            } else {
                setText(item);
                setStyle("-fx-text-fill: #1d4ed8; -fx-underline: true;");
                setCursor(Cursor.HAND);
                setOnMouseClicked(e -> {
                    LecturerMaterial m = getTableRow() == null ? null : getTableRow().getItem();
                    if (m != null) {
                        loadIntoForm(m);
                    }
                });
            }
        }
    }
}
