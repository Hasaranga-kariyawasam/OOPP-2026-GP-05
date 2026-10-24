package com.faculty.controller.to.sections;

import com.faculty.controller.to.ToUi;
import com.faculty.exception.ServiceException;
import com.faculty.model.Notice;
import com.faculty.model.to.TechnicalOfficer;
import com.faculty.service.NoticeService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Notices screen: a feed of cards, newest first (FR-64). */
public class ToNoticesController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);

    @FXML private Label noticeMessage;
    @FXML private VBox noticeBox;

    private final NoticeService service = new NoticeService();

    @FXML
    private void initialize() {
        TechnicalOfficer to = ToUi.currentOfficer();
        if (to == null) {
            return;
        }
        try {
            List<Notice> notices = service.getNotices(to.getDepId());
            if (notices.isEmpty()) {
                Label none = new Label("There are no notices for you right now.");
                none.getStyleClass().add("adm-muted");
                noticeBox.getChildren().add(none);
            }
            for (Notice n : notices) {
                noticeBox.getChildren().add(card(n));
            }
        } catch (ServiceException e) {
            ToUi.message(noticeMessage, e.getMessage(), false);
        }
    }

    private VBox card(Notice n) {
        Label title = new Label(n.getTitle());
        title.getStyleClass().add("to-notice-title");
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);

        HBox head = new HBox(10, title);
        head.setAlignment(Pos.CENTER_LEFT);
        if (n.getPostedDate().isAfter(LocalDateTime.now().minusDays(7))) {
            head.getChildren().add(ToUi.chip("New", "info"));
        }

        String audience = "ALL".equals(n.getTargetRole()) ? "Everyone" : "Technical Officers";
        Label meta = new Label(n.getPostedDate().format(FMT) + "  |  " + n.getPostedByName() + "  |  For: " + audience);
        meta.getStyleClass().add("to-notice-meta");

        Label body = new Label(n.getContent());
        body.getStyleClass().add("to-notice-body");
        body.setWrapText(true);

        VBox card = new VBox(8, head, meta, body);
        card.getStyleClass().add("adm-card");
        return card;
    }
}
