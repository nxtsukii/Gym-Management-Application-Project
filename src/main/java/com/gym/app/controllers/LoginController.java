package com.gym.app.controllers;

import java.io.IOException;
import java.util.Objects;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label validationMessage;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage("Enter both your username and password.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/frontdesk/FrontDeskLayout.fxml"),
                            "FrontDeskLayout.fxml was not found")
            );
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 760);
            scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/css/app.css"),
                    "app.css was not found"
            ).toExternalForm());

            stage.setMinWidth(980);
            stage.setMinHeight(660);
            stage.setWidth(1200);
            stage.setHeight(760);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            showMessage("Unable to open Front Desk workspace.");
            e.printStackTrace();
        }
    }

    private void showMessage(String message) {
        validationMessage.setText(message);
        validationMessage.setManaged(true);
        validationMessage.setVisible(true);
    }
}
