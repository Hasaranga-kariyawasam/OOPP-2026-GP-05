package com.faculty.controller;

import com.faculty.MainApp;
import com.faculty.exception.AuthenticationException;
import com.faculty.model.User;
import com.faculty.service.AuthService;
import com.faculty.service.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // SEC-03: validate on the GUI layer
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        try {
            User user = authService.login(username, password);
            SessionManager.setCurrentUser(user);
            // POLYMORPHISM: no if/else on role - the user object knows its own dashboard
            MainApp.switchScene(user.getDashboardFxml(), user.getDashboardTitle());

        } catch (AuthenticationException e) {
            errorLabel.setText(e.getMessage());
            passwordField.clear();
        } catch (SQLException e) {
            errorLabel.setText("Database error. Please try again.");
            System.err.println("Login DB error: " + e.getMessage());
        } catch (IOException e) {
            errorLabel.setText("Could not open the dashboard.");
            e.printStackTrace();
        }
    }
}
