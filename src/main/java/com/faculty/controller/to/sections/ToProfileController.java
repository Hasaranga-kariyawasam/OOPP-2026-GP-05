package com.faculty.controller.to.sections;

import com.faculty.MainApp;
import com.faculty.controller.to.TechnicalOfficerDashboardController;
import com.faculty.controller.to.ToUi;
import com.faculty.exception.ServiceException;
import com.faculty.exception.ValidationException;
import com.faculty.model.ProfileUpdate;
import com.faculty.model.User;
import com.faculty.service.ProfileService;
import com.faculty.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;

/** Edit own profile - never username / password (FR-13, BR-02). */
public class ToProfileController {

    @FXML private Circle bigAvatar;
    @FXML private Label bigInitials;
    @FXML private Label nameLabel;
    @FXML private Label idLabel;
    @FXML private Label pictureLabel;
    @FXML private Label usernameLabel;
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
    @FXML private Label profileMessage;

    private final ProfileService service = new ProfileService();
    private File newPicture;

    @FXML
    private void initialize() {
        fillForm();
    }

    private void fillForm() {
        User u = SessionManager.getCurrentUser();
        if (u == null) {
            return;
        }
        newPicture = null;
        pictureLabel.setText("");
        nameLabel.setText(u.getFullName());
        idLabel.setText(String.format("TO-%04d", u.getUserId()));
        usernameLabel.setText(u.getUsername());
        firstName.setText(nullToEmpty(u.getFirstName()));
        lastName.setText(nullToEmpty(u.getLastName()));
        email.setText(nullToEmpty(u.getEmail()));
        nic.setText(nullToEmpty(u.getNic()));
        dob.setValue(u.getDob());
        phone.setText(nullToEmpty(u.getPhone()));
        street.setText(nullToEmpty(u.getStreet()));
        city.setText(nullToEmpty(u.getCity()));
        postalCode.setText(nullToEmpty(u.getPostalCode()));
        country.setText(nullToEmpty(u.getCountry()));
        showAvatar(ToUi.loadImage(u.getProfilePicturePath(), 84), u);
    }

    private void showAvatar(Image image, User u) {
        if (image != null) {
            bigAvatar.setFill(new ImagePattern(image));
            bigInitials.setText("");
        } else {
            bigAvatar.setFill(Color.web("#111827"));
            String f = nullToEmpty(u.getFirstName());
            String l = nullToEmpty(u.getLastName());
            bigInitials.setText(((f.isEmpty() ? "" : f.substring(0, 1)) + (l.isEmpty() ? "" : l.substring(0, 1))).toUpperCase());
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @FXML
    private void choosePicture() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select profile picture");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(MainApp.getPrimaryStage());
        if (file != null) {
            newPicture = file;
            pictureLabel.setText("New picture: " + file.getName() + " (saved with 'Save Changes')");
            Image preview = ToUi.loadImage(file.getAbsolutePath(), 84);
            if (preview != null) {
                bigAvatar.setFill(new ImagePattern(preview));
                bigInitials.setText("");
            }
        }
    }

    @FXML
    private void save() {
        User current = SessionManager.getCurrentUser();
        if (current == null) {
            return;
        }
        ProfileUpdate input = new ProfileUpdate(firstName.getText(), lastName.getText(), email.getText(),
                nic.getText(), dob.getValue(), phone.getText(), street.getText(), city.getText(),
                postalCode.getText(), country.getText());
        try {
            User fresh = service.updateProfile(current, input, newPicture);
            SessionManager.setCurrentUser(fresh);                       // header + welcome text use this
            fillForm();
            TechnicalOfficerDashboardController.refreshProfile();
            ToUi.message(profileMessage, "Profile updated successfully.", true);
        } catch (ValidationException | ServiceException e) {
            ToUi.message(profileMessage, e.getMessage(), false);
        }
    }

    @FXML
    private void reset() {
        fillForm();
        ToUi.message(profileMessage, null, true);
    }
}
