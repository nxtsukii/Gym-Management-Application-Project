package com.gym.app.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import com.gym.app.models.Member;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MemberManagementController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    private TextField searchField;

    @FXML
    private Label feedbackBanner;

    @FXML
    private Label memberCountLabel;

    @FXML
    private TableView<Member> memberTable;

    @FXML
    private TableColumn<Member, String> idColumn;

    @FXML
    private TableColumn<Member, String> nameColumn;

    @FXML
    private TableColumn<Member, String> phoneColumn;

    @FXML
    private TableColumn<Member, String> emailColumn;

    @FXML
    private TableColumn<Member, String> typeColumn;

    @FXML
    private TableColumn<Member, String> statusColumn;

    @FXML
    private TableColumn<Member, LocalDate> expiryColumn;

    @FXML
    private Label emptyDetailsLabel;

    @FXML
    private VBox detailsBox;

    @FXML
    private Label detailIdLabel;

    @FXML
    private Label detailNameLabel;

    @FXML
    private Label detailPhoneLabel;

    @FXML
    private Label detailEmailLabel;

    @FXML
    private Label detailTypeLabel;

    @FXML
    private Label detailStatusBadge;

    @FXML
    private Label detailExpiryLabel;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    private ObservableList<Member> members = FXCollections.observableArrayList();
    private FilteredList<Member> filteredMembers;

    @FXML
    public void initialize() {
        setupTableColumns();
        memberTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        memberTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateDetailsPanel(newSelection)
        );

        bindTableData();
    }

    public void setMembers(ObservableList<Member> sharedMembers) {
        this.members = sharedMembers != null ? sharedMembers : FXCollections.observableArrayList();
        bindTableData();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().memberIdProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().membershipTypeProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().membershipStatusProperty());
        expiryColumn.setCellValueFactory(cellData -> cellData.getValue().expiryDateProperty());

        statusColumn.setCellFactory(col -> new TableCell<>() {
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
                applyStatusBadgeStyle(badge, status);
                setGraphic(badge);
                setText(null);
            }
        });

        expiryColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(date));
                }
            }
        });
    }

    private void bindTableData() {
        filteredMembers = new FilteredList<>(members, member -> true);
        SortedList<Member> sortedMembers = new SortedList<>(filteredMembers);
        sortedMembers.comparatorProperty().bind(memberTable.comparatorProperty());

        memberTable.setItems(sortedMembers);
        updateMemberCount();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();

        filteredMembers.setPredicate(member -> {
            if (query.isEmpty()) {
                return true;
            }
            return containsIgnoreCase(member.getMemberId(), query)
                    || containsIgnoreCase(member.getFullName(), query)
                    || containsIgnoreCase(member.getPhone(), query);
        });

        updateMemberCount();

        if (!query.isEmpty() && filteredMembers.isEmpty()) {
            showBanner("No members matched \"" + searchField.getText().trim() + "\".", "banner-warning");
        } else {
            hideBanner();
        }
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        filteredMembers.setPredicate(member -> true);
        updateMemberCount();
        hideBanner();
    }

    @FXML
    private void handleAddMember() {
        Member created = openMemberDialog(null);
        if (created != null) {
            members.add(created);
            handleReset();
            memberTable.getSelectionModel().select(created);
            memberTable.scrollTo(created);
            showBanner("Member " + created.getMemberId() + " (" + created.getFullName() + ") was added.",
                    "banner-success");
        }
    }

    @FXML
    private void handleEditMember() {
        Member selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showBanner("Select a member first to edit.", "banner-warning");
            return;
        }

        Member updated = openMemberDialog(selected);
        if (updated != null) {
            memberTable.refresh();
            updateDetailsPanel(updated);
            showBanner("Member " + updated.getMemberId() + " (" + updated.getFullName() + ") was updated.",
                    "banner-success");
        }
    }

    @FXML
    private void handleDeleteMember() {
        Member selected = memberTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showBanner("Select a member first to delete.", "banner-warning");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(memberTable.getScene().getWindow());
        confirm.setTitle("Delete Member");
        confirm.setHeaderText("Delete " + selected.getFullName() + " (" + selected.getMemberId() + ")?");
        confirm.setContentText("This removes the member from the current front-desk demo list.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deletedInfo = selected.getMemberId() + " (" + selected.getFullName() + ")";
            members.remove(selected);
            memberTable.getSelectionModel().clearSelection();
            updateMemberCount();
            showBanner("Member " + deletedInfo + " was deleted.", "banner-info");
        }
    }

    private Member openMemberDialog(Member memberToEdit) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource("/views/frontdesk/MemberFormDialog.fxml"),
                            "MemberFormDialog.fxml was not found")
            );
            Parent root = loader.load();

            MemberFormDialogController dialogController = loader.getController();
            if (memberToEdit == null) {
                dialogController.setupForAdd(generateNextMemberId());
            } else {
                dialogController.setupForEdit(memberToEdit);
            }

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(memberTable.getScene().getWindow());
            dialogStage.setTitle(memberToEdit == null ? "Add New Member" : "Edit Member");
            dialogStage.setResizable(false);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource("/css/app.css"),
                    "app.css was not found"
            ).toExternalForm());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            return dialogController.isSaved() ? dialogController.getResultMember() : null;
        } catch (IOException e) {
            showBanner("Unable to open member form dialog.", "banner-error");
            e.printStackTrace();
            return null;
        }
    }

    private String generateNextMemberId() {
        int maxNumber = 1000;
        for (Member member : members) {
            String id = member.getMemberId();
            if (id != null && id.startsWith("MEM-")) {
                try {
                    int number = Integer.parseInt(id.substring(4));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore non-numeric suffixes.
                }
            }
        }
        return "MEM-" + (maxNumber + 1);
    }

    private void updateDetailsPanel(Member member) {
        boolean hasSelection = member != null;

        emptyDetailsLabel.setManaged(!hasSelection);
        emptyDetailsLabel.setVisible(!hasSelection);

        detailsBox.setManaged(hasSelection);
        detailsBox.setVisible(hasSelection);

        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);

        if (!hasSelection) {
            return;
        }

        detailIdLabel.setText(member.getMemberId());
        detailNameLabel.setText(member.getFullName());
        detailPhoneLabel.setText(member.getPhone());
        detailEmailLabel.setText(member.getEmail());
        detailTypeLabel.setText(member.getMembershipType());
        detailStatusBadge.setText(member.getMembershipStatus());
        applyStatusBadgeStyle(detailStatusBadge, member.getMembershipStatus());
        detailExpiryLabel.setText(member.getExpiryDate() != null
                ? DATE_FORMATTER.format(member.getExpiryDate())
                : "-");
    }

    private void applyStatusBadgeStyle(Label label, String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase();
        switch (normalized) {
            case "active" -> label.getStyleClass().setAll("badge-active");
            case "expired" -> label.getStyleClass().setAll("badge-expired");
            case "inactive" -> label.getStyleClass().setAll("badge-inactive");
            default -> label.getStyleClass().setAll("badge-neutral");
        }
    }

    private void updateMemberCount() {
        int shown = filteredMembers == null ? 0 : filteredMembers.size();
        int total = members == null ? 0 : members.size();
        memberCountLabel.setText("Showing " + shown + " of " + total + " members");
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

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }
}
