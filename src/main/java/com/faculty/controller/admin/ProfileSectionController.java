package com.faculty.controller.admin;

import com.faculty.dao.admin.AdminDAO;
import com.faculty.model.User;
import com.faculty.service.SessionManager;
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

/** Edit Profile: personal details, profile picture and password (opened by clicking the header profile). */
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
        User u = SessionManager.getCurrentUser();
        if (u == null) return;
        if (AdminUi.text(firstField).isEmpty() || AdminUi.text(lastField).isEmpty()) {
            AdminUi.warn("First and last name are required.");
            return;
        }
        if (!AdminUi.validEmail(AdminUi.text(emailField))) {
            AdminUi.warn("Enter a valid email address.");
            return;
        }
        try {
            AdminDAO.updateProfile(u.getUserId(), AdminUi.text(firstField), AdminUi.text(lastField),
                    AdminUi.text(emailField), AdminUi.text(phoneField), AdminUi.text(nicField), dobPicker.getValue(),
                    AdminUi.text(streetField), AdminUi.text(cityField), AdminUi.text(postalField),
                    AdminUi.text(countryField), AdminUi.text(pictureField));
        } catch (SQLException e) {
            AdminUi.error(e);
            return;
        }
        // keep the logged-in session in step with the database, then refresh the header
        u.setFirstName(AdminUi.text(firstField));
        u.setLastName(AdminUi.text(lastField));
        u.setEmail(AdminUi.text(emailField));
        u.setPhone(AdminUi.text(phoneField));
        u.setNic(AdminUi.text(nicField));
        u.setDob(dobPicker.getValue());
        u.setStreet(AdminUi.text(streetField));
        u.setCity(AdminUi.text(cityField));
        u.setPostalCode(AdminUi.text(postalField));
        u.setCountry(AdminUi.text(countryField));
        u.setProfilePicturePath(AdminUi.text(pictureField));
        AdminDashboardController.refreshProfile();
        AdminUi.info("Profile updated.");
    }

    @FXML
    private void handleChangePassword() {
        User u = SessionManager.getCurrentUser();
        if (u == null) return;
        String cur = currentPw.getText() == null ? "" : currentPw.getText();
        String nw = newPw.getText() == null ? "" : newPw.getText();
        String cf = confirmPw.getText() == null ? "" : confirmPw.getText();
        if (cur.isEmpty()) { AdminUi.warn("Enter your current password."); return; }
        if (nw.length() < 6) { AdminUi.warn("New password must be at least 6 characters."); return; }
        if (!nw.equals(cf)) { AdminUi.warn("New password and confirmation do not match."); return; }
        try {
            if (AdminDAO.changePassword(u.getUserId(), cur, nw)) {
                currentPw.clear();
                newPw.clear();
                confirmPw.clear();
                AdminUi.info("Password changed.");
            } else {
                AdminUi.warn("Current password is incorrect.");
            }
        } catch (SQLException e) {
            AdminUi.error(e);
        }
    }
}
