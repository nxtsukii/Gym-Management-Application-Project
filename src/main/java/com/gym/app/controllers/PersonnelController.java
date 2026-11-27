package com.gym.app.controllers;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.UnaryOperator;

import com.gym.app.models.Personnel;
import com.gym.app.services.StubPersonnelService;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;

public class PersonnelController {

    private static final String ROLE_ALL = "All";
    private static final String ROLE_STAFF = "Staff";
    private static final String ROLE_PT = "Personal Trainer";

    private static final NumberFormat VND_FORMATTER = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    // Top Filter Bar
    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> roleFilterComboBox;

    @FXML
    private Label feedbackBanner;

    // Left/Center TableView
    @FXML
    private Label personnelCountLabel;

    @FXML
    private TableView<Personnel> personnelTable;

    @FXML
    private TableColumn<Personnel, String> idColumn;

    @FXML
    private TableColumn<Personnel, String> nameColumn;

    @FXML
    private TableColumn<Personnel, String> positionColumn;

    @FXML
    private TableColumn<Personnel, String> phoneColumn;

    @FXML
    private TableColumn<Personnel, String> specialtyShiftColumn;

    @FXML
    private TableColumn<Personnel, Double> payRateColumn;

    @FXML
    private TableColumn<Personnel, String> statusColumn;

    // Right-Side Preview/Edit Form Skeleton
    @FXML
    private Label formModeBadge;

    @FXML
    private VBox emptyFormBox;

    @FXML
    private ScrollPane formScrollPane;

    @FXML
    private TextField formPersonnelIdField;

    @FXML
    private TextField formFullNameField;

    @FXML
    private ComboBox<String> formPositionComboBox;

    @FXML
    private ComboBox<String> formStatusComboBox;

    @FXML
    private TextField formPhoneField;

    @FXML
    private TextField formPayRateField;

    @FXML
    private TextField formEmailField;

    @FXML
    private TextField formSpecialtyShiftField;

    @FXML
    private Label formValidationLabel;

    @FXML
    private Button resetFormButton;

    @FXML
    private Button saveChangesButton;

    private final StubPersonnelService personnelService = new StubPersonnelService();
    private ObservableList<Personnel> masterPersonnelList;
    private FilteredList<Personnel> filteredPersonnelList;
    private Personnel selectedPersonnel;

    @FXML
    public void initialize() {
        configureFilterControls();
        configureFormControls();
        setupTableColumns();
        bindTablePipeline();
        bindSelectionListener();
        bindInstantFilterListeners();
    }

    private void configureFilterControls() {
        roleFilterComboBox.getItems().setAll(ROLE_ALL, ROLE_STAFF, ROLE_PT);
        roleFilterComboBox.setValue(ROLE_ALL);
    }

    private void configureFormControls() {
        formPositionComboBox.getItems().setAll(ROLE_STAFF, ROLE_PT);
        formStatusComboBox.getItems().setAll("Active", "On Leave", "Inactive");

        // Restrict Pay Rate input to valid non-negative numeric values
        UnaryOperator<TextFormatter.Change> numericFilter = change -> change.getControlNewText()
                .matches("\\d*(\\.\\d*)?") ? change : null;
        formPayRateField.setTextFormatter(new TextFormatter<>(numericFilter));
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(cellData -> cellData.getValue().personnelIdProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        positionColumn.setCellValueFactory(cellData -> cellData.getValue().positionProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        specialtyShiftColumn.setCellValueFactory(cellData -> cellData.getValue().specialtyOrShiftProperty());
        payRateColumn.setCellValueFactory(cellData -> cellData.getValue().payRateProperty().asObject());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Format numeric VND pay rate cleanly while preserving numeric column sorting
        payRateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double rate, boolean empty) {
                super.updateItem(rate, empty);
                if (empty || rate == null) {
                    setText(null);
                } else {
                    setText(VND_FORMATTER.format(rate) + " VND/hr");
                }
            }
        });

        // Render status as semantic pill badges matching app.css
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

        personnelTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    // Wires ObservableList -> FilteredList -> SortedList and binds table comparator
    private void bindTablePipeline() {
        masterPersonnelList = personnelService.getAllPersonnel();
        filteredPersonnelList = new FilteredList<>(masterPersonnelList, person -> true);

        SortedList<Personnel> sortedPersonnelList = new SortedList<>(filteredPersonnelList);
        sortedPersonnelList.comparatorProperty().bind(personnelTable.comparatorProperty());

        personnelTable.setItems(sortedPersonnelList);
        updatePersonnelCount();

        masterPersonnelList.addListener((ListChangeListener<Personnel>) change -> updatePersonnelCount());
    }

    // Attaches real-time listeners so typing or changing role filters updates rows
    // immediately
    private void bindInstantFilterListeners() {
        searchField.textProperty().addListener((obs, oldQuery, newQuery) -> applyFilters());
        roleFilterComboBox.valueProperty().addListener((obs, oldRole, newRole) -> applyFilters());
    }

    // Binds TableView row selection to populate the right-side preview/edit form
    private void bindSelectionListener() {
        personnelTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> populateFormPanel(newSelection));
    }

    private void applyFilters() {
        String rawQuery = searchField.getText() == null ? "" : searchField.getText().trim();
        String normalizedQuery = rawQuery.toLowerCase(Locale.ROOT);
        String selectedRole = roleFilterComboBox.getValue() == null ? ROLE_ALL : roleFilterComboBox.getValue();

        filteredPersonnelList.setPredicate(person -> {
            if (person == null) {
                return false;
            }

            boolean matchesRole = ROLE_ALL.equalsIgnoreCase(selectedRole)
                    || (person.getPosition() != null
                            && person.getPosition().trim().equalsIgnoreCase(selectedRole));

            if (!matchesRole) {
                return false;
            }

            if (normalizedQuery.isEmpty()) {
                return true;
            }

            return containsIgnoreCase(person.getPersonnelId(), normalizedQuery)
                    || containsIgnoreCase(person.getFullName(), normalizedQuery)
                    || containsIgnoreCase(person.getPhone(), normalizedQuery)
                    || containsIgnoreCase(person.getSpecialtyOrShift(), normalizedQuery);
        });

        updatePersonnelCount();

        if (filteredPersonnelList.isEmpty() && (!normalizedQuery.isEmpty() || !ROLE_ALL.equals(selectedRole))) {
            showBanner("No personnel matched the current search or role filter.", "banner-warning");
        } else {
            hideBanner();
        }
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        roleFilterComboBox.setValue(ROLE_ALL);
        personnelTable.getSelectionModel().clearSelection();
        hideBanner();
    }

    private void populateFormPanel(Personnel person) {
        this.selectedPersonnel = person;
        hideFormValidation();

        boolean hasSelection = person != null;
        emptyFormBox.setManaged(!hasSelection);
        emptyFormBox.setVisible(!hasSelection);

        formScrollPane.setManaged(hasSelection);
        formScrollPane.setVisible(hasSelection);

        if (!hasSelection) {
            formModeBadge.setText("NO SELECTION");
            formModeBadge.getStyleClass().setAll("badge-neutral");
            return;
        }

        formModeBadge.setText("PREVIEW / EDIT");
        formModeBadge.getStyleClass().setAll("role-chip-staff");

        formPersonnelIdField.setText(safeText(person.getPersonnelId()));
        formFullNameField.setText(safeText(person.getFullName()));
        formPositionComboBox.setValue(person.getPosition());
        formStatusComboBox.setValue(person.getStatus());
        formPhoneField.setText(safeText(person.getPhone()));
        formEmailField.setText(safeText(person.getEmail()));
        formSpecialtyShiftField.setText(safeText(person.getSpecialtyOrShift()));
        formPayRateField.setText(
                BigDecimal.valueOf(person.getPayRate()).stripTrailingZeros().toPlainString());
    }

    @FXML
    private void handleResetForm() {
        if (selectedPersonnel != null) {
            populateFormPanel(selectedPersonnel);
            hideFormValidation();
        }
    }

    @FXML
    private void handleSaveChanges() {
        if (selectedPersonnel == null) {
            showFormValidation("Select a personnel record from the table first.");
            return;
        }

        String fullName = safeText(formFullNameField.getText());
        String position = formPositionComboBox.getValue();
        String status = formStatusComboBox.getValue();
        String phone = safeText(formPhoneField.getText());
        String email = safeText(formEmailField.getText());
        String specialtyOrShift = safeText(formSpecialtyShiftField.getText());
        String payRateText = safeText(formPayRateField.getText());

        if (fullName.isEmpty()) {
            showFormValidation("Full name is required.");
            formFullNameField.requestFocus();
            return;
        }
        if (position == null || status == null) {
            showFormValidation("Select both a position and status.");
            return;
        }
        if (phone.isEmpty() || !phone.matches("[0-9+\\-\\s]{8,15}")) {
            showFormValidation("Enter a valid phone number (8 to 15 digits).");
            formPhoneField.requestFocus();
            return;
        }
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showFormValidation("Enter a valid email address or leave blank.");
            formEmailField.requestFocus();
            return;
        }
        if (specialtyOrShift.isEmpty()) {
            showFormValidation("Specialty or shift assignment is required.");
            formSpecialtyShiftField.requestFocus();
            return;
        }

        double parsedPayRate;
        try {
            parsedPayRate = Double.parseDouble(payRateText);
            if (!Double.isFinite(parsedPayRate) || parsedPayRate <= 0.0) {
                showFormValidation("Pay rate must be greater than 0.");
                formPayRateField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showFormValidation("Enter a valid numeric pay rate.");
            formPayRateField.requestFocus();
            return;
        }

        // Apply validated changes to the observable model
        selectedPersonnel.setFullName(fullName);
        selectedPersonnel.setPosition(position);
        selectedPersonnel.setStatus(status);
        selectedPersonnel.setPhone(phone);
        selectedPersonnel.setEmail(email);
        selectedPersonnel.setSpecialtyOrShift(specialtyOrShift);
        selectedPersonnel.setPayRate(parsedPayRate);

        hideFormValidation();
        personnelTable.refresh();
        applyFilters();
        showBanner("Updated personnel record " + selectedPersonnel.getPersonnelId()
                + " (" + selectedPersonnel.getFullName() + ").", "banner-success");
    }

    private void applyStatusBadgeStyle(Label badge, String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "active" -> badge.getStyleClass().setAll("badge-active");
            case "on leave" -> badge.getStyleClass().setAll("badge-inactive");
            case "inactive" -> badge.getStyleClass().setAll("badge-expired");
            default -> badge.getStyleClass().setAll("badge-neutral");
        }
    }

    private void updatePersonnelCount() {
        int shown = filteredPersonnelList == null ? 0 : filteredPersonnelList.size();
        int total = masterPersonnelList == null ? 0 : masterPersonnelList.size();
        personnelCountLabel.setText("Showing " + shown + " of " + total + " personnel");
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

    private void showFormValidation(String message) {
        formValidationLabel.setText(message);
        formValidationLabel.setManaged(true);
        formValidationLabel.setVisible(true);
    }

    private void hideFormValidation() {
        formValidationLabel.setText("");
        formValidationLabel.setManaged(false);
        formValidationLabel.setVisible(false);
    }

    private boolean containsIgnoreCase(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}