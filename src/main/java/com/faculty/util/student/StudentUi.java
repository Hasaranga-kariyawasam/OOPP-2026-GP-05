package com.faculty.util.student;

import com.faculty.model.User;
import com.faculty.model.student.Undergraduate;
import com.faculty.service.SessionManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;
import java.sql.SQLException;
import java.util.Locale;
import java.util.function.Function;

/** Small helpers shared by the student screens (kept inside the student module on purpose). */
public final class StudentUi {

    private StudentUi() { }

    /** The logged-in undergraduate, or null if nobody (or a non-student) is logged in. */
    public static Undergraduate currentStudent() {
        User u = SessionManager.getCurrentUser();
        return u instanceof Undergraduate ? (Undergraduate) u : null;
    }

    // ------------------------------------------------------------------- tables

    /** Adds a plain text column to a table. */
    public static <T> TableColumn<T, String> addColumn(TableView<T> table, String title, Function<T, String> value) {
        TableColumn<T, String> col = new TableColumn<>(title);
        col.setCellValueFactory(d -> new SimpleStringProperty(value.apply(d.getValue())));
        table.getColumns().add(col);
        return col;
    }

    /** Adds a column whose text is coloured: green = good, red = bad, amber = waiting. */
    public static <T> void addStatusColumn(TableView<T> table, String title, Function<T, String> value) {
        TableColumn<T, String> col = addColumn(table, title, value);
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                String s = item.toUpperCase(Locale.ROOT);
                String color = "#111827";
                if (s.startsWith("NOT") || s.equals("REJECTED") || s.equals("ABSENT")) {
                    color = "#dc2626";
                } else if (s.equals("PENDING")) {
                    color = "#d97706";
                } else if (s.equals("ELIGIBLE") || s.equals("APPROVED") || s.equals("PRESENT")) {
                    color = "#15803d";
                }
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });
    }

    public static <T> void fitColumns(TableView<T> table) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("No records found"));
    }

    // ------------------------------------------------------------------ formats

    public static String percent(double value) {
        return String.format(Locale.ROOT, "%.1f%%", value);
    }

    public static String two(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    public static String text(String s) {
        return s == null ? "" : s.trim();
    }

    // ------------------------------------------------------------------- images

    /** Accepts a file path or a URL; returns null when there is no usable picture. */
    public static Image loadImage(String path, double size) {
        if (path == null || path.isBlank()) {
            return null;
        }
        try {
            File file = new File(path);
            String url = file.exists() ? file.toURI().toString() : path;
            Image img = new Image(url, size, size, false, true);
            return img.isError() ? null : img;
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------- alerts

    public static void info(String message) {
        show(Alert.AlertType.INFORMATION, "Information", message);
    }

    public static void warn(String message) {
        show(Alert.AlertType.WARNING, "Please check", message);
    }

    public static void error(SQLException e) {
        System.err.println("Student portal DB error: " + e.getMessage());
        show(Alert.AlertType.ERROR, "Database error", "Could not load or save data.\n\nDetails: " + e.getMessage());
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
