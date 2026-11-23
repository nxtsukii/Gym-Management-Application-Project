package com.gym.app.controllers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.gym.app.models.CheckInRecord;
import com.gym.app.models.Member;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CheckInController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private TextField searchField;

    @FXML
    private Label feedbackBanner;

    @FXML
    private Label emptyMemberLabel;

    @FXML
    private VBox memberInfoBox;

    @FXML
    private Label infoIdLabel;

    @FXML
    private Label infoNameLabel;

    @FXML
    private Label infoPhoneLabel;

    @FXML
    private Label infoTypeLabel;

    @FXML
    private Label infoStatusBadge;

    @FXML
    private Label infoExpiryLabel;

    @FXML
    private Button checkInButton;

    @FXML
    private Label historyCountLabel;

    @FXML
    private TableView<CheckInRecord> historyTable;

    @FXML
    private TableColumn<CheckInRecord, String> historyIdColumn;

    @FXML
    private TableColumn<CheckInRecord, String> historyNameColumn;

    @FXML
    private TableColumn<CheckInRecord, String> historyTypeColumn;

    @FXML
    private TableColumn<CheckInRecord, String> historyTimeColumn;

    @FXML
    private TableColumn<CheckInRecord, String> historyStatusColumn;

    private ObservableList<Member> members = FXCollections.observableArrayList();
    private ObservableList<CheckInRecord> checkInRecords = FXCollections.observableArrayList();
    private Member currentMember;

    @FXML
    public void initialize() {
        setupHistoryTable();
        historyTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    public void setSharedData(ObservableList<Member> sharedMembers,
                              ObservableList<CheckInRecord> sharedCheckInRecords) {
        this.members = sharedMembers != null ? sharedMembers : FXCollections.observableArrayList();
        this.checkInRecords = sharedCheckInRecords != null
                ? sharedCheckInRecords
                : FXCollections.observableArrayList();
        bindHistoryTable();
    }

    private void setupHistoryTable() {
        historyIdColumn.setCellValueFactory(cellData -> cellData.getValue().memberIdProperty());
        historyNameColumn.setCellValueFactory(cellData -> cellData.getValue().memberNameProperty());
        historyTypeColumn.setCellValueFactory(cellData -> cellData.getValue().membershipTypeProperty());
        historyTimeColumn.setCellValueFactory(cellData -> cellData.getValue().checkInTimeProperty());
        historyStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        historyStatusColumn.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                badge.setText(status);
                badge.getStyleClass().setAll("badge-active");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void bindHistoryTable() {
        historyTable.setItems(checkInRecords);
        updateHistoryCount();
    }

    @FXML
    private void handleSearch() {
        String memberIdQuery = searchField.getText() == null ? "" : searchField.getText().trim();

        if (memberIdQuery.isEmpty()) {
            clearMemberDisplay();
            showBanner("Enter a Member ID to search (for example, MEM-1001).", "banner-warning");
            return;
        }

        Member found = findMemberById(memberIdQuery);
        if (found == null) {
            clearMemberDisplay();
            showBanner("Member Not Found: No member exists with ID \"" + memberIdQuery + "\".", "banner-error");
            return;
        }

        currentMember = found;
        displayMemberDetails(found);
        validateMembershipForCheckIn(found);
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        clearMemberDisplay();
        hideBanner();
    }

    @FXML
    private void handleRecordCheckIn() {
        if (!isEligibleForCheckIn(currentMember)) {
            showBanner("Check-in is only allowed for members with an Active membership.", "banner-warning");
            checkInButton.setDisable(true);
            return;
        }

        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        CheckInRecord record = new CheckInRecord(
                currentMember.getMemberId(),
                currentMember.getFullName(),
                currentMember.getMembershipType(),
                timestamp,
                "Checked In"
        );

        checkInRecords.add(0, record);
        historyTable.getSelectionModel().select(record);
        historyTable.scrollTo(0);
        updateHistoryCount();

        showBanner(
                "Check-in recorded for " + currentMember.getFullName()
                        + " (" + currentMember.getMemberId() + ") at " + timestamp + ".",
                "banner-success"
        );
    }

    private Member findMemberById(String memberId) {
        for (Member member : members) {
            if (member.getMemberId() != null
                    && member.getMemberId().trim().equalsIgnoreCase(memberId)) {
                return member;
            }
        }
        return null;
    }

    private void displayMemberDetails(Member member) {
        emptyMemberLabel.setManaged(false);
        emptyMemberLabel.setVisible(false);

        memberInfoBox.setManaged(true);
        memberInfoBox.setVisible(true);

        infoIdLabel.setText(member.getMemberId());
        infoNameLabel.setText(member.getFullName());
        infoPhoneLabel.setText(member.getPhone());
        infoTypeLabel.setText(member.getMembershipType());
        infoStatusBadge.setText(member.getMembershipStatus());
        applyStatusBadgeStyle(infoStatusBadge, member.getMembershipStatus());
        infoExpiryLabel.setText(member.getExpiryDate() != null
                ? DATE_FORMATTER.format(member.getExpiryDate())
                : "-");
    }

    private void clearMemberDisplay() {
        currentMember = null;
        emptyMemberLabel.setManaged(true);
        emptyMemberLabel.setVisible(true);

        memberInfoBox.setManaged(false);
        memberInfoBox.setVisible(false);

        checkInButton.setDisable(true);
    }

    private void validateMembershipForCheckIn(Member member) {
        String status = member.getMembershipStatus() == null ? "" : member.getMembershipStatus().trim();

        if ("active".equalsIgnoreCase(status)) {
            checkInButton.setDisable(false);
            showBanner("Membership is Active and eligible for check-in.", "banner-info");
            return;
        }

        checkInButton.setDisable(true);
        if ("expired".equalsIgnoreCase(status)) {
            showBanner(
                    "Warning: " + member.getFullName() + " (" + member.getMemberId()
                            + ") has an Expired membership. Normal check-in is not allowed.",
                    "banner-warning"
            );
        } else if ("inactive".equalsIgnoreCase(status)) {
            showBanner(
                    "Warning: " + member.getFullName() + " (" + member.getMemberId()
                            + ") has an Inactive membership. Normal check-in is not allowed.",
                    "banner-warning"
            );
        } else {
            showBanner(
                    "Warning: Membership status \"" + member.getMembershipStatus()
                            + "\" is not eligible for check-in.",
                    "banner-warning"
            );
        }
    }

    private boolean isEligibleForCheckIn(Member member) {
        return member != null
                && member.getMembershipStatus() != null
                && "active".equalsIgnoreCase(member.getMembershipStatus().trim());
    }

    private void applyStatusBadgeStyle(Label label, String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "active" -> label.getStyleClass().setAll("badge-active");
            case "expired" -> label.getStyleClass().setAll("badge-expired");
            case "inactive" -> label.getStyleClass().setAll("badge-inactive");
            default -> label.getStyleClass().setAll("badge-neutral");
        }
    }

    private void updateHistoryCount() {
        int count = checkInRecords.size();
        historyCountLabel.setText(count + (count == 1 ? " record" : " records"));
    }

    private void showBanner(String message, String styleClass) {
        feedbackBanner.setText(message);
        feedbackBanner.getStyleClass().setAll(styleClass);
        feedbackBanner.setManaged(true);
        feedbackBanner.setVisible(true);
    }

    private void hideBanner() {
        feedbackBanner.setText("");
        feedbackBanner.setManaged(false);
        feedbackBanner.setVisible(false);
    }
}
