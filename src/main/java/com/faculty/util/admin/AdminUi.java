package com.faculty.util.admin;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** UI helpers shared by the admin screens. */
public final class AdminUi {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private AdminUi() { }

    /**
     * Fills a table whose cells are plain strings. Only the first headers.length values of each row are
     * shown; extra trailing values (ids, raw data) stay hidden and are read back from the selected row.
     */
    public static void fillTable(TableView<ObservableList<String>> table, String[] headers, List<List<String>> rows) {
        if (table.getColumns().size() != headers.length) {
            table.getColumns().clear();
            for (int i = 0; i < headers.length; i++) {
                final int idx = i;
                TableColumn<ObservableList<String>, String> col = new TableColumn<>(headers[i]);
                col.setCellValueFactory(d -> new SimpleStringProperty(
                        idx < d.getValue().size() ? d.getValue().get(idx) : ""));
                table.getColumns().add(col);
            }
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        }
        List<ObservableList<String>> data = new ArrayList<>();
        for (List<String> r : rows) {
            data.add(FXCollections.observableArrayList(r));
        }
        table.setItems(FXCollections.observableArrayList(data));
        table.setPlaceholder(new Label("No records found"));
    }

    public static String text(TextInputControl c) {
        return c.getText() == null ? "" : c.getText().trim();
    }

    public static boolean validEmail(String s) {
        return EMAIL.matcher(s).matches();
    }

    /** Selects the combo item with the given id (or clears the selection). */
    public static void select(ComboBox<AdminDb.Item> box, String id) {
        box.getSelectionModel().clearSelection();
        if (id == null || id.isEmpty()) {
            return;
        }
        for (AdminDb.Item it : box.getItems()) {
            if (String.valueOf(it.id()).equals(id)) {
                box.getSelectionModel().select(it);
                return;
            }
        }
    }

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
