package com.gym.app.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.gym.app.models.CheckInRecord;
import com.gym.app.models.Member;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.gym.app.utils.AlertUtil;
import com.gym.app.utils.SessionContext;

public class FrontDeskLayoutController {

    @FXML
    private Button memberManagementBtn;

    @FXML
    private Button checkInBtn;

    @FXML
    private Label headerTitleLabel;

    @FXML
    private Label headerSubtitleLabel;

    @FXML
    private StackPane contentArea;

    private final ObservableList<Member> members = FXCollections.observableArrayList();
    private final ObservableList<CheckInRecord> checkInRecords = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadSampleData();
        handleShowMemberManagement();
    }

    private void loadSampleData() {
        members.addAll(
                new Member("MEM-1001", "Nguyen Minh Anh", "0901234567", "anh.nguyen@email.com",
                        "Annual", "Active", LocalDate.now().plusMonths(8)),
                new Member("MEM-1002", "Tran Quoc Bao", "0912345678", "bao.tran@email.com",
                        "Monthly", "Active", LocalDate.now().plusDays(18)),
                new Member("MEM-1003", "Le Thu Ha", "0923456789", "ha.le@email.com",
                        "Quarterly", "Expired", LocalDate.now().minusDays(12)),
                new Member("MEM-1004", "Pham Duc Huy", "0934567890", "huy.pham@email.com",
                        "VIP", "Active", LocalDate.now().plusMonths(5)),
                new Member("MEM-1005", "Vo Khanh Linh", "0945678901", "linh.vo@email.com",
                        "Monthly", "Inactive", LocalDate.now().minusDays(45)),
                new Member("MEM-1006", "Do Hoang Nam", "0956789012", "nam.do@email.com",
                        "Quarterly", "Active", LocalDate.now().plusMonths(2)));

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        checkInRecords.addAll(
                new CheckInRecord("MEM-1001", "Nguyen Minh Anh", "Annual", today + " 07:45:00", "Checked In"),
                new CheckInRecord("MEM-1004", "Pham Duc Huy", "VIP", today + " 08:20:00", "Checked In"),
                new CheckInRecord("MEM-1002", "Tran Quoc Bao", "Monthly", today + " 09:05:00", "Checked In"));
    }

    @FXML
    private void handleShowMemberManagement() {
        setActiveNav(memberManagementBtn);
        headerTitleLabel.setText("Member Management");
        headerSubtitleLabel.setText("View, search, register, edit, and manage gym members");
        loadContentOrPlaceholder(
                "/views/frontdesk/MemberManagementView.fxml",
                "Member Management",
                "MemberManagementView.fxml will be added in Step 2.");
    }

    @FXML
    private void handleShowCheckIn() {
        setActiveNav(checkInBtn);
        headerTitleLabel.setText("Member Check-in");
        headerSubtitleLabel.setText("Search members, verify membership status, and record check-ins");
        loadContentOrPlaceholder(
                "/views/frontdesk/CheckInView.fxml",
                "Member Check-in",
                "CheckInView.fxml will be added in Step 3.");
    }

    private void loadContentOrPlaceholder(String fxmlPath, String title, String placeholderText) {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            contentArea.getChildren().setAll(createPlaceholderCard(title, placeholderText));
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof MemberManagementController memberController) {
                memberController.setMembers(members);
            } else if (controller instanceof CheckInController checkInController) {
                checkInController.setSharedData(members, checkInRecords);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            contentArea.getChildren().setAll(
                    createPlaceholderCard("Unable to load screen", e.getMessage()));
        }
    }

    private VBox createPlaceholderCard(String title, String message) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("card-subtitle");

        VBox card = new VBox(8.0, titleLabel, messageLabel);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(460.0);
        card.setMaxHeight(160.0);
        card.getStyleClass().add("card");
        return card;
    }

    private void setActiveNav(Button activeButton) {
        memberManagementBtn.getStyleClass().setAll(
                activeButton == memberManagementBtn ? "nav-button-active" : "nav-button");
        checkInBtn.getStyleClass().setAll(
                activeButton == checkInBtn ? "nav-button-active" : "nav-button");
    }

    @FXML
    private void handleLogout() {
        SessionContext.clear();
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getRe                "LoginView.fxml was not found")
            );
            Parent root = loader.load();
            Stage stage = (Stage) contentArea.getScene().getWindow();

            Scene scene = new Scene(root, 900, 620);
            scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource(        "app.css was not found"
            ).toExternalForm());

            stage.setMinWidth(520);
            stage.setMinHeight(500);
            stage.setWidth(900);
            stage.setHeight(620);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            AlertUtil.showError(
                    contentArea.getScene() != null ? contentArea.getScene().getWindow() : null,
                    "Logout Error",
                    "Unable to return to the login screen."
            );
        }
    }

    public ObservableList<Member> getMembers() {
        return members;
    }

    public ObservableList<CheckInRecord> getCheckInRecords() {
        return checkInRecords;
    }
}
