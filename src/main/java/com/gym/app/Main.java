package com.gym.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(Main.class.getResource("/views/common/LoginView.fxml"),
                        "LoginView.fxml was not found"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 900, 620);
        scene.getStylesheets().add(Objects.requireNonNull(
                Main.class.getResource("/css/app.css"),
                "app.css was not found").toExternalForm());

        stage.setTitle("Gym Management System");
        stage.setMinWidth(520);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}