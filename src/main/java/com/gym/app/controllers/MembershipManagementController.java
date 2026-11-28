package com.gym.app.controllers;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gym.app.models.MembershipPackage;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MembershipManagementController {

    private static final Logger LOGGER = Logger.getLogger(MembershipManagementController.class.getName());
    private static final String ALL_STATUSES = "All";
    private static final NumberFormat VND_FORMATTER =
            NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private Label feedbackBanner;

    @FXML
    private Label packageCountLabel;

    @FXML
    private TableView<MembershipPackage> packageTable;

    @FXML
    private TableColumn<MembershipPackage, String> idColumn;

    @FXML
    private TableColumn<MembershipPackage, String> nameColumn;

    @FXML
    private TableColumn<MembershipPackage, String> durationColumn;

    @FXML
    private TableColumn<MembershipPackage, Double> priceColumn;

    @FXML
    private TableColumn<MembershipPackage, String> descriptionColumn;

    @FXML
    private TableColumn<MembershipPackage, Boolean> statusColumn;

    @FXML
    private TableColumn<MembershipPackage, Void> actionsColumn;

    private ObservableList<MembershipPackage> packages;
    private FilteredList<MembershipPackage> filteredPackages;
    private final ListChangeListener<MembershipPackage> packageListListener =
            change -> updatePackageCount();

    @FXML
    public void initialize() {
        statusFilterComboBox.getItems().setAll(ALL_STATUSES, "Active", "Inactive");
        statusFilterComboBox.setValue(ALL_STATUSES);
        setupTableColumns();
        packageTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    }

    public void setMembershipPackages(ObservableList<MembershipPackage> sharedPackages) {
        if (packages != null) {
            packages.removeListener(packageListListener);
        }
        packages = Objects.requireNonNull(sharedPackages, "Shared membership packages must not be null.");
        bindTableData();
        packages.addListener(packageListListener);
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().packageIdProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().packageNameProperty());
        durationColumn.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(cellData.getValue().getFormattedDuration()));
        priceColumn.setCellValueFactory(cellData -> cellData.getValue().priceProperty().asObject());
        descriptionColumn.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().activeProperty());

        priceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : VND_FORMATTER.format(price));
                setAlignment(Pos.CENTER_RIGHT);
            }
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();

            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                badge.setText(active ? "Active" : "Inactive");
                badge.getStyleClass().setAll(active ? "badge-active" : "badge-inactive");
                setGraphic(badge);
                setText(null);
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button toggleButton = new Button();
            private final HBox buttons = new HBox(6.0, editButton, toggleButton);

            {
                editButton.getStyleClass().setAll("secondary-button");
                editButton.setMinWidth(Region.USE_PREF_SIZE);
                toggleButton.setText("Deactivate");
                toggleButton.getStyleClass().setAll("secondary-button");
                toggleButton.setMinWidth(Region.USE_PREF_SIZE);
                toggleButton.setOnAction(event -> {
                    MembershipPackage membershipPackage = getRowPackage();
                    if (membershipPackage != null) {
                        packageTable.getSelectionModel().select(membershipPackage);
                        togglePackageStatus(membershipPackage);
                    }
                });
                editButton.setOnAction(event -> {
                    MembershipPackage membershipPackage = getRowPackage();
                    if (membershipPackage != null) {
                        packageTable.getSelectionModel().select(membershipPackage);
                        openPackageDialog(membershipPackage);
                    }
                });
            }

            private MembershipPackage getRowPackage() {
                return getTableRow() == null ? null : getTableRow().getItem();
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                MembershipPackage membershipPackage =
                        empty || getTableRow() == null ? null : getTableRow().getItem();
                if (membershipPackage == null) {
                    setGraphic(null);
                    return;
                }

                boolean active = membershipPackage.isActive();
                toggleButton.setText(active ? "Deactivate" : "Activate");
                toggleButton.getStyleClass().setAll(active ? "secondary-button" : "success-button");
                setGraphic(buttons);
            }
        });
    }

    private void bindTableData() {
        filteredPackages = new FilteredList<>(packages, membershipPackage -> true);
        SortedList<MembershipPackage> sortedPackages = new SortedList<>(filteredPackages);
        sortedPackages.comparatorProperty().bind(packageTable.comparatorProperty());
        packageTable.setItems(sortedPackages);
        updatePackageCount();
    }

    @FXML
    private void handleSearch() {
        applyFilters();
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        statusFilterComboBox.setValue(ALL_STATUSES);
        applyFilters();
    }

    @FXML
    private void handleAddPackage() {
        openPackageDialog(null);
    }

    private void openPackageDialog(MembershipPackage packageToEdit) {
        if (packages == null) {
            showBanner("Membership package data is unavailable.", "banner-error");
            return;
        }

        URL dialogResource = getClass().getResource("/views/frontdesk/MembershipFormDialog.fxml");
        if (dialogResource == null) {
            LOGGER.severe("MembershipFormDialog.fxml was not found.");
            showBanner("Unable to open the Add Package form.", "banner-error");
            return;
        }
        URL stylesheet = getClass().getResource("/css/app.css");
        if (stylesheet == null) {
            LOGGER.severe("app.css was not found.");
            showBanner("Unable to open the Add Package form.", "banner-error");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(dialogResource);
            Parent root = loader.load();

            MembershipFormDialogController dialogController = loader.getController();
            if (packageToEdit == null) {
                dialogController.setupForAdd(packages);
            } else {
                dialogController.setupForEdit(packageToEdit, packages);
            }

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(packageTable.getScene().getWindow());
            dialogStage.setTitle(packageToEdit == null
                    ? "Add Membership Package"
                    : "Edit Membership Package");
            dialogStage.setResizable(false);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(stylesheet.toExternalForm());

            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            if (dialogController.isSaved()) {
                MembershipPackage savedPackage = dialogController.getResultPackage();
                if (packageToEdit != null) {
                    refreshFilteredPackages();
                    packageTable.refresh();
                }
                showBanner("Membership package " + savedPackage.getPackageName()
                        + (packageToEdit == null ? " was added." : " was updated."),
                        "banner-success");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to open MembershipFormDialog.fxml.", e);
            showBanner(packageToEdit == null
                    ? "Unable to open the Add Package form."
                    : "Unable to open the Edit Package form.", "banner-error");
        }
    }

    private void applyFilters() {
        refreshFilteredPackages();
        if (filteredPackages.isEmpty()) {
            showBanner("No membership packages matched the current search and filter.", "banner-warning");
        } else {
            hideBanner();
        }
    }

    private void refreshFilteredPackages() {
        String query = searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String selectedStatus = statusFilterComboBox.getValue();

        filteredPackages.setPredicate(membershipPackage -> {
            boolean matchesSearch = query.isEmpty()
                    || containsIgnoreCase(membershipPackage.getPackageId(), query)
                    || containsIgnoreCase(membershipPackage.getPackageName(), query);
            boolean matchesStatus = ALL_STATUSES.equals(selectedStatus)
                    || ("Active".equals(selectedStatus) && membershipPackage.isActive())
                    || ("Inactive".equals(selectedStatus) && !membershipPackage.isActive());
            return matchesSearch && matchesStatus;
        });

        updatePackageCount();
    }

    private void togglePackageStatus(MembershipPackage membershipPackage) {
        if (membershipPackage.isActive()) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.initOwner(packageTable.getScene().getWindow());
            confirmation.setTitle("Deactivate Membership Package");
            confirmation.setHeaderText("Deactivate " + membershipPackage.getPackageName() + "?");
            confirmation.setContentText(
                    "This package will no longer be available for new member assignments. "
                            + "Existing members assigned to it will retain their current membership type.");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }

            membershipPackage.setActive(false);
            refreshFilteredPackages();
            packageTable.refresh();
            showBanner("Membership package " + membershipPackage.getPackageName()
                    + " was deactivated. Existing members were not changed.",
                    "banner-success");
            return;
        }

        membershipPackage.setActive(true);
        refreshFilteredPackages();
        packageTable.refresh();
        showBanner("Membership package " + membershipPackage.getPackageName()
                + " was activated and is available for new member assignments.",
                "banner-success");
    }

    private void updatePackageCount() {
        int shown = filteredPackages == null ? 0 : filteredPackages.size();
        int total = packages == null ? 0 : packages.size();
        packageCountLabel.setText("Showing " + shown + " of " + total + " packages");
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
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }
}
