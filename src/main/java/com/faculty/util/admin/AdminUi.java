package com.faculty.util.admin;

import com.faculty.model.admin.AdminOption;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.function.Function;

/** JavaFX helpers shared by the admin screens. UI only: no SQL and no business rules in here. */
public final class AdminUi {

    private AdminUi() { }

    // ------------------------------------------------------------------ tables

    /** Call once in initialize(): removes old columns and sets the resize policy and the empty text. */
    public static <T> void prepare(TableView<T> table) {
        table.getColumns().clear();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No records found"));
    }

    /** Adds a text column; value says what to show for one row. */
    public static <T> void column(TableView<T> table, String header, Function<T, String> value) {
        TableColumn<T, String> col = new TableColumn<>(header);
        col.setCellValueFactory(d -> new SimpleStringProperty(value.apply(d.getValue())));
        table.getColumns().add(col);
    }

    public static <T> void show(TableView<T> table, List<T> rows) {
        table.setItems(FXCollections.observableArrayList(rows));
    }

    // ------------------------------------------------------------------- inputs

    public static String text(TextInputControl c) {
        return c.getText() == null ? "" : c.getText().trim();
    }

    /** Selects the combo item with the given id (or clears the selection when id is null). */
    public static void select(ComboBox<AdminOption> box, Integer id) {
        box.getSelectionModel().clearSelection();
        if (id == null) {
            return;
        }
        for (AdminOption it : box.getItems()) {
            if (it.id() == id) {
                box.getSelectionModel().select(it);
                return;
            }
        }
    }

    /** Id of the chosen option, or 0 when nothing is chosen. */
    public static int idOf(ComboBox<AdminOption> box) {
        AdminOption o = box.getValue();
        return o == null ? 0 : o.id();
    }

    // ------------------------------------------------------------------- dialogs

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Done", message);
    }

    public static void warn(String message) {
        show(Alert.AlertType.WARNING, "Check your input", message);
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Error", message);
    }

    public static void error(SQLException e) {
        String msg;
        if (e instanceof SQLIntegrityConstraintViolationException) {
            String m = e.getMessage() == null ? "" : e.getMessage();
            if (m.contains("Duplicate")) {
                msg = "That value already exists (duplicate username, email, ID or code).";
            } else {
                msg = "This record is linked to other data, or a required value is invalid.";
            }
        } else {
            msg = "Database error: " + e.getMessage();
        }
        error(msg);
    }

    public static boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        a.setTitle("Please confirm");
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type, message, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }
}
