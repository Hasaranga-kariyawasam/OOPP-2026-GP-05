package com.faculty.controller.lecture;

import com.faculty.model.lecture.Lecturer;
import com.faculty.service.LecturerProfileService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;

/** "Edit Profile" screen: the lecturer changes everything except username and password. */
public class LecturerProfileController {

    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label designationLabel;
    @FXML private Label messageLabel;

    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField email;
    @FXML private TextField nic;
    @FXML private DatePicker dob;
    @FXML private TextField phone;
    @FXML private TextField street;
    @FXML private TextField city;
    @FXML private TextField postalCode;
    @FXML private TextField country;

    private final LecturerProfileService service = new LecturerProfileService();

    @FXML
    private void initialize() {
        try {
            fill(service.getCurrent());
        } catch (SecurityException e) {
            showMessage("Please log in again.", false);
        }
    }

    private void fill(Lecturer me) {
        nameLabel.setText(me.getFullName());
        usernameLabel.setText(me.getUsername());
        designationLabel.setText(me.getDesignation() == null ? "Lecturer" : me.getDesignation());
        firstName.setText(nz(me.getFirstName()));
        lastName.setText(nz(me.getLastName()));
        email.setText(nz(me.getEmail()));
        nic.setText(nz(me.getNic()));
        dob.setValue(me.getDob());
        phone.setText(nz(me.getPhone()));
        street.setText(nz(me.getStreet()));
        city.setText(nz(me.getCity()));
        postalCode.setText(nz(me.getPostalCode()));
        country.setText(nz(me.getCountry()));
    }

    @FXML
    private void handleSave() {
        try {
            service.save(firstName.getText(), lastName.getText(), email.getText(), nic.getText(),
                    dob.getValue(), phone.getText(), street.getText(), city.getText(),
                    postalCode.getText(), country.getText());
            fill(service.getCurrent());
            showMessage("Profile saved. Your name in the top bar updates the next time you log in.", true);
        } catch (IllegalArgumentException e) {
            showMessage(e.getMessage(), false);
        } catch (SQLException | SecurityException e) {
            System.err.println("Saving profile failed: " + e.getMessage());
            showMessage("Could not save your profile. Please try again later.", false);
        }
    }

    @FXML
    private void handleReset() {
        fill(service.getCurrent());
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
    }

    private void showMessage(String text, boolean ok) {
        messageLabel.setText(text);
        messageLabel.setStyle("-fx-text-fill: " + (ok ? "#15803d" : "#b91c1c") + ";");
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
