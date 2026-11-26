package com.gym.app.controllers;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.function.UnaryOperator;

import com.gym.app.models.MembershipPackage;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

public class MembershipFormDialogController {

    @FXML
    private TextField packageIdField;

    @FXML
    private Label dialogTitleLabel;

    @FXML
    private Label dialogSubtitleLabel;

    @FXML
    private TextField packageNameField;

    @FXML
    private TextField durationField;

    @FXML
    private TextField priceField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private Label validationLabel;

    @FXML
    private Button saveButton;

    private ObservableList<MembershipPackage> sharedPackages;
    private MembershipPackage editingPackage;
    private MembershipPackage resultPackage;
    private boolean saved;

    @FXML
    public void initialize() {
        UnaryOperator<TextFormatter.Change> wholeNumberFilter =
                change -> change.getControlNewText().matches("\\d*") ? change : null;
        durationField.setTextFormatter(new TextFormatter<>(wholeNumberFilter));

        UnaryOperator<TextFormatter.Change> decimalFilter =
                change -> change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null;
        priceField.setTextFormatter(new TextFormatter<>(decimalFilter));
    }

    public void setupForAdd(ObservableList<MembershipPackage> packages) {
        sharedPackages = Objects.requireNonNull(packages, "Shared membership packages must not be null.");
        editingPackage = null;
        resultPackage = null;
        saved = false;
        dialogTitleLabel.setText("Add Membership Package");
        dialogSubtitleLabel.setText("Enter the new package details below.");
        saveButton.setText("Save Package");
        packageIdField.clear();
        packageNameField.clear();
        durationField.clear();
        priceField.clear();
        descriptionArea.clear();
    }

    public void setupForEdit(MembershipPackage membershipPackage,
                             ObservableList<MembershipPackage> packages) {
        sharedPackages = Objects.requireNonNull(packages, "Shared membership packages must not be null.");
        editingPackage = Objects.requireNonNull(membershipPackage, "Package to edit must not be null.");
        resultPackage = null;
        saved = false;
        dialogTitleLabel.setText("Edit Membership Package");
        dialogSubtitleLabel.setText("Update the selected package details below.");
        saveButton.setText("Save Changes");
        packageIdField.setText(membershipPackage.getPackageId());
        packageNameField.setText(membershipPackage.getPackageName());
        durationField.setText(Integer.toString(membershipPackage.getDurationMonths()));
        priceField.setText(BigDecimal.valueOf(membershipPackage.getPrice())
                .stripTrailingZeros()
                .toPlainString());
        descriptionArea.setText(membershipPackage.getDescription());
    }

    @FXML
    private void handleSave() {
        String packageId = packageIdField.getText() == null ? "" : packageIdField.getText().trim();
        String packageName = packageNameField.getText() == null ? "" : packageNameField.getText().trim();
        String durationText = durationField.getText() == null ? "" : durationField.getText().trim();
        String priceText = priceField.getText() == null ? "" : priceField.getText().trim();

        if (packageId.isEmpty()) {
            showValidation("Package ID is required.");
            packageIdField.requestFocus();
            return;
        }
        if (packageName.isEmpty()) {
            showValidation("Package name is required.");
            packageNameField.requestFocus();
            return;
        }
        if (hasDuplicateId(packageId)) {
            showValidation("A package with this ID already exists.");
            packageIdField.requestFocus();
            return;
        }
        if (hasDuplicateName(packageName)) {
            showValidation("A package with this name already exists.");
            packageNameField.requestFocus();
            return;
        }

        int durationMonths;
        try {
            durationMonths = Integer.parseInt(durationText);
        } catch (NumberFormatException e) {
            showValidation("Duration must be a positive whole number of months.");
            durationField.requestFocus();
            return;
        }
        if (durationMonths <= 0) {
            showValidation("Duration must be greater than zero months.");
            durationField.requestFocus();
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceText);
        } catch (NumberFormatException e) {
            showValidation("Price must be a valid positive number.");
            priceField.requestFocus();
            return;
        }
        double priceValue = price.doubleValue();
        if (price.signum() <= 0 || priceValue <= 0.0 || !Double.isFinite(priceValue)) {
            showValidation("Price must be greater than zero and within the supported range.");
            priceField.requestFocus();
            return;
        }

        if (editingPackage == null) {
            resultPackage = new MembershipPackage(
                    packageId,
                    packageName,
                    durationMonths,
                    priceValue,
                    descriptionArea.getText(),
                    true);
            sharedPackages.add(resultPackage);
        } else {
            editingPackage.setPackageId(packageId);
            editingPackage.setPackageName(packageName);
            editingPackage.setDurationMonths(durationMonths);
            editingPackage.setPrice(priceValue);
            editingPackage.setDescription(descriptionArea.getText());
            resultPackage = editingPackage;
        }
        saved = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        saved = false;
        resultPackage = null;
        closeDialog();
    }

    public boolean isSaved() {
        return saved;
    }

    public MembershipPackage getResultPackage() {
        return resultPackage;
    }

    private boolean hasDuplicateId(String packageId) {
        return sharedPackages.stream()
                .anyMatch(existing -> existing != editingPackage
                        && existing.getPackageId().equalsIgnoreCase(packageId));
    }

    private boolean hasDuplicateName(String packageName) {
        return sharedPackages.stream()
                .anyMatch(existing -> existing != editingPackage
                        && existing.getPackageName().equalsIgnoreCase(packageName));
    }

    private void showValidation(String message) {
        validationLabel.setText(message);
        validationLabel.setManaged(true);
        validationLabel.setVisible(true);
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}
