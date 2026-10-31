package com.faculty.controller.student;

import com.faculty.model.student.Undergraduate;
import com.faculty.service.student.StudentService;
import com.faculty.util.student.StudentUi;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Student profile. A student may change ONLY the contact details (e-mail, phone, address)
 * and the profile picture; every other detail is shown read-only.
 */
public class StudentProfileController {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9 ]{9,15}$");
    private static final Path PICTURE_DIR = Path.of("uploads", "profiles");

    @FXML private Circle avatar;
    @FXML private Label initialsLabel;
    @FXML private Label nameLabel;
    @FXML private Label regLabel;

    @FXML private Label usernameVal;
    @FXML private Label regVal;
    @FXML private Label batchVal;
    @FXML private Label statusVal;
    @FXML private Label nicVal;
    @FXML private Label dobVal;
    @FXML private Label intakeVal;

    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField streetField;
    @FXML private TextField cityField;
    @FXML private TextField postalField;
    @FXML private TextField countryField;

    private final StudentService service = new StudentService();

    @FXML
    private void initialize() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        nameLabel.setText(s.getFullName());
        regLabel.setText(StudentUi.text(s.getRegNumber()));
        usernameVal.setText(StudentUi.text(s.getUsername()));
        regVal.setText(StudentUi.text(s.getRegNumber()));
        batchVal.setText(StudentUi.text(s.getBatch()));
        statusVal.setText(StudentUi.text(s.getStatus()).replace('_', ' '));
        dobVal.setText(s.getDob() == null ? "" : s.getDob().toString());
        intakeVal.setText(s.getIntakeDate() == null ? "" : s.getIntakeDate().toString());
        fillContactFields(s);
        showAvatar(s);
    }

    private void fillContactFields(Undergraduate s) {
        emailField.setText(StudentUi.text(s.getEmail()));
        phoneField.setText(StudentUi.text(s.getPhone()));
        streetField.setText(StudentUi.text(s.getStreet()));
        cityField.setText(StudentUi.text(s.getCity()));
        postalField.setText(StudentUi.text(s.getPostalCode()));
        countryField.setText(StudentUi.text(s.getCountry()));
    }

    private void showAvatar(Undergraduate s) {
        Image photo = StudentUi.loadImage(s.getProfilePicturePath(), 88);
        if (photo != null) {
            avatar.setFill(new ImagePattern(photo));
            initialsLabel.setVisible(false);
        } else {
            avatar.setFill(Color.web("#111827"));
            String f = StudentUi.text(s.getFirstName());
            String l = StudentUi.text(s.getLastName());
            String initials = (f.isEmpty() ? "" : f.substring(0, 1)) + (l.isEmpty() ? "" : l.substring(0, 1));
            initialsLabel.setText(initials.toUpperCase());
            initialsLabel.setVisible(true);
        }
    }

    // ------------------------------------------------------------------ actions

    @FXML
    private void handleChoosePicture() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose profile picture");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File chosen = fc.showOpenDialog(avatar.getScene().getWindow());
        if (chosen == null) {
            return;
        }
        try {
            // keep our own copy so the picture still works if the original file is moved
            Files.createDirectories(PICTURE_DIR);
            String name = chosen.getName();
            String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : ".png";
            Path target = PICTURE_DIR.resolve("student_" + s.getUserId() + "_" + System.currentTimeMillis() + ext);
            Files.copy(chosen.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            service.dao().updatePicture(s.getUserId(), target.toString());
            s.setProfilePicturePath(target.toString());
            showAvatar(s);
            StudentDashboardController.refreshProfile();
            StudentUi.info("Profile picture updated.");
        } catch (IOException e) {
            StudentUi.warn("Could not copy the picture file. Please choose another image.");
        } catch (SQLException e) {
            StudentUi.error(e);
        }
    }

    @FXML
    private void handleSave() {
        Undergraduate s = StudentUi.currentStudent();
        if (s == null) {
            return;
        }
        String email = emailField.getText() == null ? "" : emailField.getText().trim();
        String phone = StudentUi.text(phoneField.getText());
        String street = StudentUi.text(streetField.getText());
        String city = StudentUi.text(cityField.getText());
        String postal = StudentUi.text(postalField.getText());
        String country = StudentUi.text(countryField.getText());

        if (!EMAIL.matcher(email).matches() || email.length() > 100) {
            StudentUi.warn("Enter a valid email address.");
            return;
        }
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            StudentUi.warn("Phone number must have 9 to 15 digits (a leading + is allowed).");
            return;
        }
        if (street.length() > 100 || city.length() > 50 || postal.length() > 10 || country.length() > 50) {
            StudentUi.warn("One of the address fields is too long.");
            return;
        }

        try {
            boolean ok = service.dao().updateContact(s.getUserId(), email, phone, street, city, postal, country);
            if (!ok) {
                StudentUi.warn("That email address is already used by another account.");
                return;
            }
        } catch (SQLException e) {
            StudentUi.error(e);
            return;
        }

        // keep the logged-in session in step with the database
        s.setEmail(email);
        s.setPhone(phone);
        s.setStreet(street);
        s.setCity(city);
        s.setPostalCode(postal);
        s.setCountry(country);
        StudentUi.info("Contact details saved.");
    }

    @FXML
    private void handleReset() {
        Undergraduate s = StudentUi.currentStudent();
        if (s != null) {
            fillContactFields(s);
        }
    }
}
