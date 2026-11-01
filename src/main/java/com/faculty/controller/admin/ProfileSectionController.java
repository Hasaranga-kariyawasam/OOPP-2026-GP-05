package com.faculty.controller.admin;

import com.faculty.exception.AdminValidationException;
import com.faculty.model.User;
import com.faculty.model.admin.AdminProfileData;
import com.faculty.service.SessionManager;
import com.faculty.service.admin.AdminProfileService;
import com.faculty.util.admin.AdminUi;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;

/** Edit Profile: personal details, profile picture and password (opened by clicking the header profile). UI only. */
public class ProfileSectionController {

    @FXML private Circle avatar;
    @FXML private Label initialsLabel;
    @FXML private Label usernameLabel;
    @FXML private Label roleLabel;
    @FXML private TextField firstField;
    @FXML private TextField lastField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField nicField;
    @FXML private DatePicker dobPicker;
    @FXML private TextField streetField;
    @FXML private TextField cityField;
    @FXML private TextField postalField;
    @FXML private TextField countryField;
    @FXML private TextField pictureField;
    @FXML private PasswordField currentPw;
    @FXML private PasswordField newPw;
    @FXML private PasswordField confirmPw;

    private final AdminProfileService profileService = new AdminProfileService();

    @FXML
    private void initialize() {
        User u = SessionManager.getCurrentUser();
        if (u == null) {
            return;
        }
        usernameLabel.setText("Username: " + u.getUsername());
        roleLabel.setText(u.getRole() == null ? "" : u.getRole().name().replace('_', ' '));
        firstField.setText(nz(u.getFirstName()));
        lastField.setText(nz(u.getLastName()));
        emailField.setText(nz(u.getEmail()));
        phoneField.setText(nz(u.getPhone()));
        nicField.setText(nz(u.getNic()));
        dobPicker.setValue(u.getDob());
        streetField.setText(nz(u.getStreet()));
        cityField.setText(nz(u.getCity()));
        postalField.setText(nz(u.getPostalCode()));
        countryField.setText(nz(u.getCountry()));
        pictureField.setText(nz(u.getProfilePicturePath()));
        showAvatar();
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }

    private void showAvatar() {
        String path = AdminUi.text(pictureField);
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
            String f = AdminUi.text(firstField);
            String l = AdminUi.text(lastField);
            initialsLabel.setText(((f.isEmpty() ? "" : f.substring(0, 1)) + (l.isEmpty() ? "" : l.substring(0, 1))).toUpperCase());
            initialsLabel.setVisible(true);
        }
    }

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

    @FXML
    private void handleSave() {
        AdminProfileData data = new AdminProfileData(AdminUi.text(firstField), AdminUi.text(lastField),
                AdminUi.text(emailField), AdminUi.text(phoneField), AdminUi.text(nicField), dobPicker.getValue(),
                AdminUi.text(streetField), AdminUi.text(cityField), AdminUi.text(postalField),
                AdminUi.text(countryField), AdminUi.text(pictureField));
        try {
            profileService.saveProfile(data);
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
            return;
        } catch (SQLException e) {
            AdminUi.error(e);
            return;
        }
        AdminDashboardController.refreshProfile();      // the header shows the new name / picture
        AdminUi.info("Profile updated.");
    }

    @FXML
    private void handleChangePassword() {
        try {
            profileService.changePassword(currentPw.getText(), newPw.getText(), confirmPw.getText());
            currentPw.clear();
            newPw.clear();
            confirmPw.clear();
            AdminUi.info("Password changed.");
        } catch (AdminValidationException e) {
            AdminUi.warn(e.getMessage());
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
