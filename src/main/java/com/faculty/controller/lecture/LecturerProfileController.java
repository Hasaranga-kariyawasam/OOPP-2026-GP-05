package com.faculty.controller.lecture;

import com.faculty.model.lecture.Lecturer;
import com.faculty.service.LecturerProfileService;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;

import java.sql.SQLException;

/** "Edit Profile" screen: the lecturer changes everything except username and password. */
public class LecturerProfileController {

    @FXML private Label nameLabel;
    @FXML private Label usernameLabel;
    @FXML private Label designationLabel;
    @FXML private Label messageLabel;
    @FXML private Circle avatar;
    @FXML private Label initialsLabel;
    @FXML private TextField pictureField;

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
        pictureField.setText(nz(me.getProfilePicturePath()));
        showAvatar();
    }

    @FXML
    private void handleSave() {
        try {
            service.save(firstName.getText(), lastName.getText(), email.getText(), nic.getText(),
                    dob.getValue(), phone.getText(), street.getText(), city.getText(),
                    postalCode.getText(), country.getText(), pictureField.getText());
            fill(service.getCurrent());
            LecturerDashboardController.refreshProfile();      // top bar shows the new name / picture
            showMessage("Profile saved.", true);
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

    // ------------------------------------------------------------ profile picture

    @FXML
    private void handleChoosePicture() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose profile picture");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(avatar.getScene().getWindow());
        if (f != null) {
            pictureField.setText(f.getAbsolutePath());
            showAvatar();
        }
    }

    @FXML
    private void handleRemovePicture() {
        pictureField.clear();
        showAvatar();
    }

    /** Shows the chosen picture in the circle, or the initials when there is none (or it cannot be read). */
    private void showAvatar() {
        String path = pictureField.getText() == null ? "" : pictureField.getText().trim();
        Image img = null;
        if (!path.isEmpty()) {
            try {
                File f = new File(path);
                img = new Image(f.exists() ? f.toURI().toString() : path, 120, 120, false, true);
                if (img.isError()) {
                    img = null;
                }
            } catch (Exception e) {
                img = null;
            }
        }
        if (img != null) {
            avatar.setFill(new ImagePattern(img));
            initialsLabel.setVisible(false);
        } else {
            avatar.setFill(Color.web("#111827"));
            String f = firstName.getText() == null ? "" : firstName.getText().trim();
            String l = lastName.getText() == null ? "" : lastName.getText().trim();
            String text = (f.isEmpty() ? "" : f.substring(0, 1)) + (l.isEmpty() ? "" : l.substring(0, 1));
            initialsLabel.setText(text.isEmpty() ? "?" : text.toUpperCase());
            initialsLabel.setVisible(true);
        }
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
