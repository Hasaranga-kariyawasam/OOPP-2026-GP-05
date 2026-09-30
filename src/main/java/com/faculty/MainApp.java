package com.faculty;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/** Application entry point. Opens the login screen; controllers call switchScene() to navigate. */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        switchScene("/fxml/login.fxml", "Faculty Management System - Login");
        stage.show();
    }

    /** Replaces the window content with another FXML screen. */
    public static void switchScene(String fxmlPath, String title) throws IOException {
        Parent root = FXMLLoader.load(MainApp.class.getResource(fxmlPath));
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
