package com.faculty.controller.to;

import com.faculty.MainApp;
import com.faculty.model.User;
import com.faculty.model.to.TechnicalOfficer;
import com.faculty.service.SessionManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;

import java.io.File;
import java.util.Optional;
import java.util.function.Function;

/** Small GUI helpers shared by every Technical Officer screen (messages, dialogs, chips, pictures). */
public final class ToUi {

    private ToUi() { }

    /** The logged-in Technical Officer, or null when somebody else (or nobody) is logged in. */
    public static TechnicalOfficer currentOfficer() {
        User u = SessionManager.getCurrentUser();
        return (u instanceof TechnicalOfficer) ? (TechnicalOfficer) u : null;
    }

    /** Shows a green (ok) or red (error) message in an inline label; null / empty clears it. */
    public static void message(Label label, String text, boolean ok) {
        label.getStyleClass().removeAll("to-msg-ok", "to-msg-err");
        if (text == null || text.isEmpty()) {
            label.setText("");
            return;
        }
        label.getStyleClass().add(ok ? "to-msg-ok" : "to-msg-err");
        label.setText(text);
    }

    /** Yes / No question before a destructive action. */
    public static boolean confirm(String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Please confirm");
        if (MainApp.getPrimaryStage() != null) {
            alert.initOwner(MainApp.getPrimaryStage());
        }
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static void info(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, text, ButtonType.OK);
        alert.setHeaderText(null);
        if (MainApp.getPrimaryStage() != null) {
            alert.initOwner(MainApp.getPrimaryStage());
        }
        alert.showAndWait();
    }

    /** A rounded status label, e.g. chip("Approved", "ok"). Kinds: ok, bad, warn, neutral, info. */
    public static Label chip(String text, String kind) {
        Label label = new Label(text);
        label.getStyleClass().addAll("to-chip", "to-chip-" + kind);
        return label;
    }

    /** 73.3333 -> "73.3%"; no sessions recorded -> "-". */
    public static String percent(double value, boolean hasData) {
        return hasData ? String.format("%.1f%%", value) : "-";
    }

    /** Accepts a file path or URL; returns null when there is no usable picture. */
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

    /** A text column: title, preferred width and a function that reads the text from the row object. */
    public static <S> TableColumn<S, String> column(String title, double width, Function<S, String> getter) {
        TableColumn<S, String> col = new TableColumn<>(title);
        col.setPrefWidth(width);
        col.setCellValueFactory(cd -> new SimpleStringProperty(getter.apply(cd.getValue())));
        return col;
    }

    /** Turns a text column into a column of coloured chips; kindOf maps the text to ok / bad / warn / neutral / info. */
    public static <S> void chipColumn(TableColumn<S, String> column, Function<String, String> kindOf) {
        column.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else if (item.equals("-")) {
                    setGraphic(null);
                    setText("-");
                } else {
                    setText(null);
                    setGraphic(chip(item, kindOf.apply(item)));
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });
    }

    /** Chip colour for a medical status text: Approved = ok, Pending = warn, Not Approved = bad. */
    public static String approvalKind(String label) {
        switch (label) {
            case "Approved": return "ok";
            case "Pending":  return "warn";
            default:         return "bad";
        }
    }

    /** REGULAR -> "Regular", BATCH_MISSED -> "Batch Missed". */
    public static String studentStatus(String status) {
        if (status == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String part : status.toLowerCase().split("_")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
