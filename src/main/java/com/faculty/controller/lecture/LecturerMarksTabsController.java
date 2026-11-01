package com.faculty.controller.lecture;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;

/** Container of the marks screen: reloads the Grades and GPA tabs whenever they are opened, so they show the latest marks. */
public class LecturerMarksTabsController {

    @FXML private Tab gradesTab;
    @FXML private Tab gpaTab;

    // injected by FXMLLoader from <fx:include fx:id="grades"> and <fx:include fx:id="gpa">
    @FXML private LecturerGradesController gradesController;
    @FXML private LecturerGpaController gpaController;

    @FXML
    private void initialize() {
        gradesTab.setOnSelectionChanged(e -> {
            if (gradesTab.isSelected()) {
                gradesController.load();
            }
        });
        gpaTab.setOnSelectionChanged(e -> {
            if (gpaTab.isSelected()) {
                gpaController.load();
            }
        });
    }
}
