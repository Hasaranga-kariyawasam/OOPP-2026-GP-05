package com.faculty.controller.lecture;

import com.faculty.model.lecture.LecturerNotice;
import com.faculty.service.LecturerNoticeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** "Notices" screen: read-only list of the notices for lecturers; click a row to read it. */
public class LecturerNoticesController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    @FXML private Label countLabel;
    @FXML private Label messageLabel;

    @FXML private TableView<LecturerNotice> table;
    @FXML private TableColumn<LecturerNotice, String> colDate;
    @FXML private TableColumn<LecturerNotice, String> colTitle;
    @FXML private TableColumn<LecturerNotice, String> colBy;

    @FXML private Label dTitle;
    @FXML private Label dMeta;
    @FXML private TextArea dContent;

    private final LecturerNoticeService service = new LecturerNoticeService();

    @FXML
    private void initialize() {
        colDate.setCellValueFactory(c -> new SimpleStringProperty(date(c.getValue())));
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));
        colBy.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPostedBy()));
        table.setPlaceholder(new Label("There are no notices for you right now."));

        table.getSelectionModel().selectedItemProperty().addListener((o, a, n) -> showNotice(n));
        showNotice(null);

        try {
            List<LecturerNotice> list = service.getMyNotices();
            table.setItems(FXCollections.observableArrayList(list));
            countLabel.setText(list.size() + (list.size() == 1 ? " notice" : " notices"));
            if (!list.isEmpty()) {
                table.getSelectionModel().selectFirst();
            }
        } catch (SQLException | SecurityException e) {      // NFR-04: show a message, never crash
            System.err.println("Loading notices failed: " + e.getMessage());
            messageLabel.setText("Could not load notices. Please try again later.");
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }

    private void showNotice(LecturerNotice n) {
        if (n == null) {
            dTitle.setText("Select a notice from the list");
            dMeta.setText("");
            dContent.clear();
            return;
        }
        dTitle.setText(n.getTitle());
        dMeta.setText(date(n) + "  |  " + n.getPostedBy());
        dContent.setText(n.getContent());
    }

    private String date(LecturerNotice n) {
        return n.getPostedDate() == null ? "-" : n.getPostedDate().format(DATE_FMT);
    }
}
