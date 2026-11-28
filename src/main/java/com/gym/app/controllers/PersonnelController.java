package com.gym.app.controllers;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.UnaryOperator;

import com.gym.app.models.Personnel;
import com.gym.app.services.StubPersonnelService;
import com.gym.app.utils.AlertUtil;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class PersonnelController {

    private static final String ROLE_ALL = "All";
    private static final String ROLE_STAFF = "Staff";
    private static final String ROLE_PT = "Personal Trainer";

    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_ON_LEAVE = "On Leave";
    private static final String STATUS_INACTIVE = "Inactive";

    private static final String ERROR_STYLE_CLASS = "input-field-error";
    private static final NumberFormat VND_FORMATTER = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private enum FormMode {
        NONE,
        CREATE,
        EDIT
    }

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

    // Right-Side CRUD Form
    @FXML
    private Label formModeBadge;

    @FXML
    private Button newPersonnelButton;

    @FXML
    private VBox emptyFormBox;

    @FXML
    private ScrollPane formScrollPane;

    @FXML
    private TextField formPersonnelIdField;

    @FXML
    private TextField formFullNameField;

    @FXML
    private Label nameErrorLabel;

    @FXML
    private ComboBox<String> formPositionComboBox;

    @FXML
    private Label positionErrorLabel;

    @FXML
    private ComboBox<String> formStatusComboBox;

    @FXML
    private Label statusErrorLabel;

    @FXML
    private TextField formPhoneField;

    @FXML
    private Label phoneErrorLabel;

    @FXML
    private TextField formPayRateField;

    @FXML
    private Label payRateErrorLabel;

    @FXML
    private TextField formEmailField;

    @FXML
    private Label emailErrorLabel;

    @FXML
    private VBox specialtyFieldContainer;

    @FXML
    private TextField formSpecialtyShiftField;

    @FXML
    private Label specialtyErrorLabel;

    @FXML
    private Label formValidationLabel;

    @FXML
    private Button clearFormButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    private final StubPersonnelService personnelService = new StubPersonnelService();
    private ObservableList<Personnel> masterPersonnelList;
    private FilteredList<Personnel> filteredPersonnelList;

    private FormMode currentMode = FormMode.NONE;
    private Personnel selectedPersonnel;

    @FXML
    public void initialize() {
        configureFilterControls();
        configureFormControls();
        setupTableColumns();
        bindTablePipeline();
        bindSelectionListener();
        bindInstantFilterListeners();
        bindConditionalAndValidationListeners();
        enterEmptyState();
    }

    private void configureFilterControls() {
        roleFilterComboBox.getItems().setAll(ROLE_ALL, ROLE_STAFF, ROLE_PT);
        roleFilterComboBox.setValue(ROLE_ALL);
    }

    private void configureFormControls() {
        formPositionComboBox.getItems().setAll(ROLE_STAFF, ROLE_PT);
        formStatusComboBox.getItems().setAll(STATUS_ACTIVE, STATUS_ON_LEAVE, STATUS_INACTIVE);

        // Restrict Pay Rate input to non-negative decimal numbers at keystroke level
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

        // Render "-" cleanly when a Staff record has no Personal Trainer specialty
        specialtyShiftColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(value == null || value.isBlank() ? "-" : value);
                }
            }
        });

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

    // Re-evaluates active keyword & role filters immediately on user input
    private void bindInstantFilterListeners() {
        searchField.textProperty().addListener((obs, oldQuery, newQuery) -> applyFilters());
        roleFilterComboBox.valueProperty().addListener((obs, oldRole, newRole) -> applyFilters());
    }

    // Syncs TableView row selection to the right-hand Edit/Delete form
    private void bindSelectionListener() {
        personnelTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        enterEditMode(newSelection);
                    } else if (currentMode == FormMode.EDIT) {
                        enterEmptyState();
                    }
                });
    }

    // Wires dynamic Specialty field toggling and auto-clearing inline error states
    private void bindConditionalAndValidationListeners() {
        formPositionComboBox.valueProperty().addListener((obs, oldPos, newPos) -> {
            clearFieldError(formPositionComboBox, positionErrorLabel);
            hideFormValidation();
            updateSpecialtyFieldVisibility(newPos);
        });

        formStatusComboBox.valueProperty().addListener((obs, oldStatus, newStatus) -> {
            clearFieldError(formStatusComboBox, statusErrorLabel);
            hideFormValidation();
        });

        formFullNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(formFullNameField, nameErrorLabel);
            hideFormValidation();
        });

        formPhoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(formPhoneField, phoneErrorLabel);
            hideFormValidation();
        });

        formPayRateField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(formPayRateField, payRateErrorLabel);
            hideFormValidation();
        });

        formEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(formEmailField, emailErrorLabel);
            hideFormValidation();
        });

        formSpecialtyShiftField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearFieldError(formSpecialtyShiftField, specialtyErrorLabel);
            hideFormValidation();
        });
    }

    /**
     * Dynamically shows and enables the Specialty field ONLY when Position is
     * set to "Personal Trainer". Collapses and disables it for "Staff" or null.
     */
    private void updateSpecialtyFieldVisibility(String position) {
        boolean isPersonalTrainer = ROLE_PT.equalsIgnoreCase(safeText(position));

        specialtyFieldContainer.setVisible(isPersonalTrainer);
        specialtyFieldContainer.setManaged(isPersonalTrainer);
        formSpecialtyShiftField.setDisable(!isPersonalTrainer);

        if (!isPersonalTrainer) {
            clearFieldError(formSpecialtyShiftField, specialtyErrorLabel);
        }
    }

    private void applyFilters() {
        if (filteredPersonnelList == null) {
            return;
        }

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
        } else if (feedbackBanner.isVisible() && feedbackBanner.getStyleClass().contains("banner-warning")) {
            hideBanner();
        }
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        roleFilterComboBox.setValue(ROLE_ALL);
        personnelTable.getSelectionModel().clearSelection();
        enterEmptyState();
        hideBanner();
    }

    // Enters Create mode with an auto-generated unique Personnel ID
    @FXML
    private void handleNewPersonnel() {
        personnelTable.getSelectionModel().clearSelection();
        selectedPersonnel = null;
        currentMode = FormMode.CREATE;

        clearAllFieldErrors();
        hideFormValidation();

        emptyFormBox.setManaged(false);
        emptyFormBox.setVisible(false);
        formScrollPane.setManaged(true);
        formScrollPane.setVisible(true);

        formModeBadge.setText("NEW RECORD");
        formModeBadge.getStyleClass().setAll("badge-active");
        saveButton.setText("Create Record");

        // Hide Delete button when creating an unsaved record
        deleteButton.setDisable(true);
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);

        formPersonnelIdField.setText(generateNextPersonnelId());
        clearEditableInputs();
        formPositionComboBox.setValue(ROLE_STAFF);
        formStatusComboBox.setValue(STATUS_ACTIVE);
        updateSpecialtyFieldVisibility(ROLE_STAFF);

        formFullNameField.requestFocus();
    }

    // Clears all editable inputs and validation states in the right-hand form
    @FXML
    private void handleClearForm() {
        clearAllFieldErrors();
        hideFormValidation();
        clearEditableInputs();

        if (currentMode == FormMode.CREATE) {
            formPositionComboBox.setValue(ROLE_STAFF);
            formStatusComboBox.setValue(STATUS_ACTIVE);
            updateSpecialtyFieldVisibility(ROLE_STAFF);
        } else {
            formPositionComboBox.setValue(null);
            formStatusComboBox.setValue(null);
            updateSpecialtyFieldVisibility(null);
        }

        formFullNameField.requestFocus();
    }

    private void clearEditableInputs() {
        formFullNameField.clear();
        formPhoneField.clear();
        formPayRateField.clear();
        formEmailField.clear();
        formSpecialtyShiftField.clear();
    }

    private void enterEmptyState() {
        currentMode = FormMode.NONE;
        selectedPersonnel = null;
        clearAllFieldErrors();
        hideFormValidation();

        emptyFormBox.setManaged(true);
        emptyFormBox.setVisible(true);
        formScrollPane.setManaged(false);
        formScrollPane.setVisible(false);

        deleteButton.setDisable(true);
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);

        formModeBadge.setText("NO SELECTION");
        formModeBadge.getStyleClass().setAll("badge-neutral");
    }

    private void enterEditMode(Personnel person) {
        currentMode = FormMode.EDIT;
        selectedPersonnel = person;
        clearAllFieldErrors();
        hideFormValidation();

        emptyFormBox.setManaged(false);
        emptyFormBox.setVisible(false);
        formScrollPane.setManaged(true);
        formScrollPane.setVisible(true);

        formModeBadge.setText("EDITING");
        formModeBadge.getStyleClass().setAll("role-chip-staff");
        saveButton.setText("Save Changes");

        deleteButton.setDisable(false);
        deleteButton.setVisible(true);
        deleteButton.setManaged(true);

        formPersonnelIdField.setText(safeText(person.getPersonnelId()));
        formFullNameField.setText(safeText(person.getFullName()));
        formPositionComboBox.setValue(person.getPosition());
        formStatusComboBox.setValue(person.getStatus());
        formPhoneField.setText(safeText(person.getPhone()));
        formPayRateField.setText(
                BigDecimal.valueOf(person.getPayRate()).stripTrailingZeros().toPlainString());
        formEmailField.setText(safeText(person.getEmail()));

        updateSpecialtyFieldVisibility(person.getPosition());
        if (ROLE_PT.equalsIgnoreCase(safeText(person.getPosition()))) {
            formSpecialtyShiftField.setText(safeText(person.getSpecialtyOrShift()));
        } else {
            formSpecialtyShiftField.clear();
        }
    }

    // Handles both Create and Update after strict inline validation
    @FXML
    private void handleSavePersonnel() {
        if (currentMode == FormMode.NONE) {
            showFormValidation("Click '+ New' to add a record or select an existing row to edit.");
            return;
        }
        if (currentMode == FormMode.EDIT && selectedPersonnel == null) {
            showFormValidation("Select a personnel record from the table first.");
            return;
        }

        if (!validateFormInputs()) {
            return;
        }

        String personnelId = safeText(formPersonnelIdField.getText());
        String fullName = safeText(formFullNameField.getText());
        String position = formPositionComboBox.getValue();
        String status = formStatusComboBox.getValue();
        String phone = safeText(formPhoneField.getText());
        String email = safeText(formEmailField.getText());
        double payRate = Double.parseDouble(safeText(formPayRateField.getText()));
        String specialty = ROLE_PT.equalsIgnoreCase(position)
                ? safeText(formSpecialtyShiftField.getText())
                : "";

        if (currentMode == FormMode.CREATE) {
            if (personnelService.isDuplicateId(personnelId, null)) {
                personnelId = generateNextPersonnelId();
                formPersonnelIdField.setText(personnelId);
            }

            Personnel created = new Personnel(
                    personnelId,
                    fullName,
                    position,
                    phone,
                    email,
                    specialty,
                    payRate,
                    status);

            masterPersonnelList.add(created);
            applyFilters();

            if (filteredPersonnelList.contains(created)) {
                personnelTable.getSelectionModel().select(created);
                personnelTable.scrollTo(created);
                showBanner("Created " + position + " record " + created.getPersonnelId()
                        + " (" + created.getFullName() + ").", "banner-success");
            } else {
                enterEmptyState();
                showBanner("Created " + created.getPersonnelId() + " (" + created.getFullName()
                        + "), currently hidden by the active search/role filter.", "banner-info");
            }
            return;
        }

        // Update existing record
        selectedPersonnel.setFullName(fullName);
        selectedPersonnel.setPosition(position);
        selectedPersonnel.setStatus(status);
        selectedPersonnel.setPhone(phone);
        selectedPersonnel.setEmail(email);
        selectedPersonnel.setSpecialtyOrShift(specialty);
        selectedPersonnel.setPayRate(payRate);

        Personnel updatedRecord = selectedPersonnel;
        personnelTable.refresh();
        applyFilters();

        if (filteredPersonnelList.contains(updatedRecord)) {
            personnelTable.getSelectionModel().select(updatedRecord);
            showBanner("Updated personnel record " + updatedRecord.getPersonnelId()
                    + " (" + updatedRecord.getFullName() + ").", "banner-success");
        } else {
            personnelTable.getSelectionModel().clearSelection();
            enterEmptyState();
            showBanner("Updated " + updatedRecord.getPersonnelId() + " (" + updatedRecord.getFullName()
                    + "); record is now hidden by the active search/role filter.", "banner-info");
        }
    }

    // Handles Deactivate / Delete gated behind
    // AlertUtil.showConfirmation(...)
    @FXML
    private void handleDeletePersonnel() {
        if (currentMode != FormMode.EDIT || selectedPersonnel == null) {
            showFormValidation("Select a personnel record from the table before deleting.");
            return;
        }

        Personnel target = selectedPersonnel;
        boolean confirmed = AlertUtil.showConfirmation(
                resolveWindow(),
                "Delete Personnel Record",
                "Delete " + target.getFullName() + " (" + target.getPersonnelId() + ")?",
                "This removes the " + target.getPosition() + " record from the in-memory directory. "
                        + "If you only wish to suspend access temporarily, change Status to 'Inactive' and click Save Changes.");

        if (!confirmed) {
            return;
        }

        String deletedSummary = target.getPersonnelId() + " (" + target.getFullName() + ")";
        masterPersonnelList.remove(target);
        personnelTable.getSelectionModel().clearSelection();
        enterEmptyState();
        applyFilters();

        showBanner("Deleted personnel record " + deletedSummary + ".", "banner-info");
    }

    /**
     * Validates all form fields prior to Create/Update, applying inline red error
     * borders (.input-field-error) and field-level error messages.
     */
    private boolean validateFormInputs() {
        clearAllFieldErrors();
        hideFormValidation();

        Control firstInvalidControl = null;
        int errorCount = 0;

        String fullName = safeText(formFullNameField.getText());
        if (fullName.isEmpty()) {
            markFieldError(formFullNameField, nameErrorLabel, "Full name is required.");
            firstInvalidControl = formFullNameField;
            errorCount++;
        } else if (!fullName.matches(".*[\\p{L}].*")) {
            markFieldError(formFullNameField, nameErrorLabel, "Full name must include alphabetic characters.");
            firstInvalidControl = formFullNameField;
            errorCount++;
        }

        String position = formPositionComboBox.getValue();
        if (position == null || position.isBlank()) {
            markFieldError(formPositionComboBox, positionErrorLabel, "Select a position.");
            if (firstInvalidControl == null) {
                firstInvalidControl = formPositionComboBox;
            }
            errorCount++;
        }

        String status = formStatusComboBox.getValue();
        if (status == null || status.isBlank()) {
            markFieldError(formStatusComboBox, statusErrorLabel, "Select a status.");
            if (firstInvalidControl == null) {
                firstInvalidControl = formStatusComboBox;
            }
            errorCount++;
        }

        String phone = safeText(formPhoneField.getText());
        String digitsOnly = phone.replaceAll("\\D", "");
        if (phone.isEmpty()) {
            markFieldError(formPhoneField, phoneErrorLabel, "Phone number is required.");
            if (firstInvalidControl == null) {
                firstInvalidControl = formPhoneField;
            }
            errorCount++;
        } else if (!phone.matches("[0-9+\\-\\s]+") || digitsOnly.length() < 8 || digitsOnly.length() > 15) {
            markFieldError(formPhoneField, phoneErrorLabel, "Enter a valid phone number (8–15 digits).");
            if (firstInvalidControl == null) {
                firstInvalidControl = formPhoneField;
            }
            errorCount++;
        } else if (isDuplicatePhone(digitsOnly, selectedPersonnel)) {
            markFieldError(formPhoneField, phoneErrorLabel, "Another personnel record already uses this phone.");
            if (firstInvalidControl == null) {
                firstInvalidControl = formPhoneField;
            }
            errorCount++;
        }

        String payRateText = safeText(formPayRateField.getText());
        if (payRateText.isEmpty()) {
            markFieldError(formPayRateField, payRateErrorLabel, "Pay rate is required.");
            if (firstInvalidControl == null) {
                firstInvalidControl = formPayRateField;
            }
            errorCount++;
        } else {
            try {
                double parsedPayRate = Double.parseDouble(payRateText);
                if (!Double.isFinite(parsedPayRate) || parsedPayRate < 0.0) {
                    markFieldError(formPayRateField, payRateErrorLabel, "Pay rate must be 0 or greater.");
                    if (firstInvalidControl == null) {
                        firstInvalidControl = formPayRateField;
                    }
                    errorCount++;
                }
            } catch (NumberFormatException e) {
                markFieldError(formPayRateField, payRateErrorLabel, "Enter a valid numeric pay rate.");
                if (firstInvalidControl == null) {
                    firstInvalidControl = formPayRateField;
                }
                errorCount++;
            }
        }

        String email = safeText(formEmailField.getText());
        if (!email.isEmpty() && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            markFieldError(formEmailField, emailErrorLabel, "Enter a valid email address or leave blank.");
            if (firstInvalidControl == null) {
                firstInvalidControl = formEmailField;
            }
            errorCount++;
        }

        // Specialty is required ONLY when Position is set to Personal Trainer
        if (ROLE_PT.equalsIgnoreCase(safeText(position))) {
            String specialty = safeText(formSpecialtyShiftField.getText());
            if (specialty.isEmpty()) {
                markFieldError(formSpecialtyShiftField, specialtyErrorLabel,
                        "Specialty is required for Personal Trainers.");
                if (firstInvalidControl == null) {
                    firstInvalidControl = formSpecialtyShiftField;
                }
                errorCount++;
            }
        }

        if (errorCount > 0) {
            showFormValidation("Please fix the highlighted "
                    + (errorCount == 1 ? "field" : errorCount + " fields") + " before saving.");
            if (firstInvalidControl != null) {
                firstInvalidControl.requestFocus();
            }
            return false;
        }

        return true;
    }

    private boolean isDuplicatePhone(String candidateDigits, Personnel excludedRecord) {
        return masterPersonnelList.stream()
                .filter(existing -> existing != excludedRecord)
                .map(existing -> safeText(existing.getPhone()).replaceAll("\\D", ""))
                .anyMatch(existingDigits -> !existingDigits.isEmpty() && existingDigits.equals(candidateDigits));
    }

    private String generateNextPersonnelId() {
        int maxNumber = 1000;
        for (Personnel person : masterPersonnelList) {
            String id = person.getPersonnelId();
            if (id != null && id.toUpperCase(Locale.ROOT).startsWith("PER-")) {
                try {
                    int number = Integer.parseInt(id.substring(4).trim());
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException ignored) {
                    // Skip non-numeric suffixes
                }
            }
        }
        return "PER-" + (maxNumber + 1);
    }

    private void markFieldError(Control control, Label inlineErrorLabel, String message) {
        if (!control.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            control.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        inlineErrorLabel.setText(message);
        inlineErrorLabel.setManaged(true);
        inlineErrorLabel.setVisible(true);
    }

    private void clearFieldError(Control control, Label inlineErrorLabel) {
        control.getStyleClass().remove(ERROR_STYLE_CLASS);
        inlineErrorLabel.setText("");
        inlineErrorLabel.setManaged(false);
        inlineErrorLabel.setVisible(false);
    }

    private void clearAllFieldErrors() {
        clearFieldError(formFullNameField, nameErrorLabel);
        clearFieldError(formPositionComboBox, positionErrorLabel);
        clearFieldError(formStatusComboBox, statusErrorLabel);
        clearFieldError(formPhoneField, phoneErrorLabel);
        clearFieldError(formPayRateField, payRateErrorLabel);
        clearFieldError(formEmailField, emailErrorLabel);
        clearFieldError(formSpecialtyShiftField, specialtyErrorLabel);
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

    private Window resolveWindow() {
        return personnelTable != null && personnelTable.getScene() != null
                ? personnelTable.getScene().getWindow()
                : null;
    }

    private boolean containsIgnoreCase(String value, String normalizedQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }
}