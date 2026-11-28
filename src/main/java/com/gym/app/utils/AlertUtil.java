package com.gym.app.utils;

import java.net.URL;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Window;

public final class AlertUtil {

    private static final String APP_STYLESHEET = "/css/app.css";

    private AlertUtil() {
        // Utility class
    }

    public static void showInfo(String title, String content) {
        showInfo(resolveActiveWindow(), title, null, content);
    }

    public static void showInfo(Window owner, String title, String content) {
        showInfo(owner, title, null, content);
    }

    public static void showInfo(Window owner, String title, String header, String content) {
        Alert alert = buildAlert(Alert.AlertType.INFORMATION, owner, title, header, content);
        alert.showAndWait();
    }

    public static void showWarning(String title, String content) {
        showWarning(resolveActiveWindow(), title, null, content);
    }

    public static void showWarning(Window owner, String title, String content) {
        showWarning(owner, title, null, content);
    }

    public static void showWarning(Window owner, String title, String header, String content) {
        Alert alert = buildAlert(Alert.AlertType.WARNING, owner, title, header, content);
        alert.showAndWait();
    }

    public static void showError(String title, String content) {
        showError(resolveActiveWindow(), title, null, content);
    }

    public static void showError(Window owner, String title, String content) {
        showError(owner, title, null, content);
    }

    public static void showError(Window owner, String title, String header, String content) {
        Alert alert = buildAlert(Alert.AlertType.ERROR, owner, title, header, content);
        alert.showAndWait();
    }

    // Displays a blocking confirmation dialog and returns true only if the user
    // clicks OK
    public static boolean showConfirmation(String title, String content) {
        return showConfirmation(resolveActiveWindow(), title, null, content);
    }

    public static boolean showConfirmation(Window owner, String title, String content) {
        return showConfirmation(owner, title, null, content);
    }

    public static boolean showConfirmation(Window owner, String title, String header, String content) {
        Alert alert = buildAlert(Alert.AlertType.CONFIRMATION, owner, title, header, content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static Alert buildAlert(Alert.AlertType type,
            Window owner,
            String title,
            String header,
            String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title != null ? title : defaultTitleFor(type));
        alert.setHeaderText(header);
        alert.setContentText(content != null ? content : "");
        alert.initModality(Modality.APPLICATION_MODAL);

        Window resolvedOwner = owner != null ? owner : resolveActiveWindow();
        if (resolvedOwner != null) {
            alert.initOwner(resolvedOwner);
        }

        DialogPane dialogPane = alert.getDialogPane();
        URL cssUrl = AlertUtil.class.getResource(APP_STYLESHEET);
        if (cssUrl != null) {
            dialogPane.getStylesheets().add(cssUrl.toExternalForm());
        }
        dialogPane.setMinWidth(380.0);

        return alert;
    }

    private static Window resolveActiveWindow() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    private static String defaultTitleFor(Alert.AlertType type) {
        return switch (type) {
            case INFORMATION -> "Information";
            case WARNING -> "Warning";
            case ERROR -> "Error";
            case CONFIRMATION -> "Confirm Action";
            default -> "Notice";
        };
    }
}