package com.gym.app.controllers;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import com.gym.app.services.StubAuthService;
import com.gym.app.services.StubAuthService.AuthenticatedUser;
import com.gym.app.services.StubAuthService.TestCredential;
import com.gym.app.utils.AlertUtil;
import com.gym.app.utils.SessionContext;
import com.gym.app.utils.SessionContext.Role;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.stage.Stage;

public class LoginController {

    private static final String ERROR_STYLE_CLASS = "input-field-error";
    private static final String WORKSPACE_FXML = "/views/common/MainLayout.fxml";
    private static final String APP_CSS = "/css/app.css";

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label validationMessage;

    @FXML
    private Button loginButton;

    @FXML
    private Button ownerDemoButton;

    @FXML
    private Button staffDemoButton;

    private final StubAuthService authService = new StubAuthService();
    private boolean navigating;

    @FXML
    public void initialize() {
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(usernameField);
            hideValidationMessage();
        });
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(passwordField);
            hideValidationMessage();
        });
    }

    @FXML
    private void handleLogin() {
        if (navigating) {
            return;
        }

        String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String password = passwordField.getText() == null ? "" : passwordField.getText();

        boolean usernameEmpty = username.isEmpty();
        boolean passwordEmpty = password.isEmpty();

        if (usernameEmpty || passwordEmpty) {
            if (usernameEmpty) {
                markFieldError(usernameField);
            }
            if (passwordEmpty) {
                markFieldError(passwordField);
            }

            if (usernameEmpty && passwordEmpty) {
                showValidationMessage("Enter both your username and password.");
                usernameField.requestFocus();
            } else if (usernameEmpty) {
                showValidationMessage("Username is required.");
                usernameField.requestFocus();
            } else {
                showValidationMessage("Password is required.");
                passwordField.requestFocus();
            }
            return;
        }

        Optional<AuthenticatedUser> authenticatedUser = authService.authenticate(username, password);
        if (authenticatedUser.isEmpty()) {
            markFieldError(usernameField);
            markFieldError(passwordField);
            passwordField.clear();
            passwordField.requestFocus();
            showValidationMessage("Invalid username or password. Use a demo button below if testing.");
            return;
        }

        AuthenticatedUser user = authenticatedUser.get();
        SessionContext.startSession(user.userId(), user.displayName(), user.role());
        navigateToWorkspace();
    }

    @FXML
    private void handleOwnerQuickLogin() {
        fillAndSubmitRole(Role.OWNER);
    }

    @FXML
    private void handleStaffQuickLogin() {
        fillAndSubmitRole(Role.STAFF);
    }

    private void fillAndSubmitRole(Role role) {
        if (navigating) {
            return;
        }
        TestCredential credential = authService.getTestCredentialForRole(role);
        usernameField.setText(credential.username());
        passwordField.setText(credential.password());
        handleLogin();
    }

    private void navigateToWorkspace() {
        navigating = true;
        setControlsDisabled(true);

        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(WORKSPACE_FXML),
                            "FrontDeskLayout.fxml was not found"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 1200, 760);
            scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource(APP_CSS),
                    "app.css was not found").toExternalForm());

            stage.setMinWidth(980);
            stage.setMinHeight(660);
            stage.setWidth(1200);
            stage.setHeight(760);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException e) {
            navigating = false;
            setControlsDisabled(false);
            SessionContext.clear();
            showValidationMessage("Unable to open the workspace. Please try again.");
            AlertUtil.showError(
                    usernameField.getScene() != null ? usernameField.getScene().getWindow() : null,
                    "Navigation Error",
                    "Failed to load the workspace screen: " + e.getMessage());
        }
    }

    private void setControlsDisabled(boolean disabled) {
        loginButton.setDisable(disabled);
        ownerDemoButton.setDisable(disabled);
        staffDemoButton.setDisable(disabled);
    }

    private void markFieldError(TextInputControl field) {
        if (!field.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            field.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    private void clearFieldError(TextInputControl field) {
        field.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    private void showValidationMessage(String message) {
        validationMessage.setText(message);
        validationMessage.setManaged(true);
        validationMessage.setVisible(true);
    }

    private void hideValidationMessage() {
        if (validationMessage.isVisible()) {
            validationMessage.setText("");
            validationMessage.setManaged(false);
            validationMessage.setVisible(false);
        }
    }
}
