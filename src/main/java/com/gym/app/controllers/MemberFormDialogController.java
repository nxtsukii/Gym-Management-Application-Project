package com.gym.app.controllers;

import java.time.LocalDate;

import com.gym.app.models.Member;
import com.gym.app.models.MembershipPackage;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class MemberFormDialogController {

    @FXML
    private Label dialogTitleLabel;

    @FXML
    private Label dialogSubtitleLabel;

    @FXML
    private TextField memberIdField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private DatePicker expiryDatePicker;

    @FXML
    private Label validationLabel;

    @FXML
    private Button saveButton;

    private Member editingMember;
    private Member resultMember;
    private boolean saved;

    @FXML
    public void initialize() {
        statusComboBox.getItems().setAll("Active", "Expired", "Inactive");
    }

    public void setupForAdd(String nextMemberId, ObservableList<MembershipPackage> sharedPackages) {
        this.editingMember = null;
        dialogTitleLabel.setText("Add New Member");
        dialogSubtitleLabel.setText("Enter the new member details below.");
        saveButton.setText("Add Member");

        memberIdField.setText(nextMemberId);
        populatePackageChoices(sharedPackages, null);
        statusComboBox.setValue("Active");
        expiryDatePicker.setValue(LocalDate.now().plusMonths(1));
    }

    public void setupForEdit(Member member, ObservableList<MembershipPackage> sharedPackages) {
        this.editingMember = member;
        dialogTitleLabel.setText("Edit Member");
        dialogSubtitleLabel.setText("Update member details or renew membership.");
        saveButton.setText("Save Changes");

        memberIdField.setText(member.getMemberId());
        fullNameField.setText(member.getFullName());
        phoneField.setText(member.getPhone());
        emailField.setText(member.getEmail());
        populatePackageChoices(sharedPackages, member.getMembershipType());
        statusComboBox.setValue(member.getMembershipStatus());
        expiryDatePicker.setValue(member.getExpiryDate());
    }

    private void populatePackageChoices(ObservableList<MembershipPackage> sharedPackages,
                                       String existingPackageName) {
        typeComboBox.getItems().clear();

        if (sharedPackages != null) {
            for (MembershipPackage membershipPackage : sharedPackages) {
                String packageName = membershipPackage.getPackageName();
                if (membershipPackage.isActive()
                        && packageName != null
                        && !typeComboBox.getItems().contains(packageName)) {
                    typeComboBox.getItems().add(packageName);
                }
            }
        }

        if (existingPackageName != null && !typeComboBox.getItems().contains(existingPackageName)) {
            typeComboBox.getItems().add(existingPackageName);
        }

        if (existingPackageName != null) {
            typeComboBox.setValue(existingPackageName);
        } else if (!typeComboBox.getItems().isEmpty()) {
            typeComboBox.setValue(typeComboBox.getItems().get(0));
        } else {
            typeComboBox.setValue(null);
        }
    }

    @FXML
    private void handleSave() {
        String id = memberIdField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String type = typeComboBox.getValue();
        String status = statusComboBox.getValue();
        LocalDate expiryDate = expiryDatePicker.getValue();

        if (fullName.isEmpty()) {
            showValidation("Full name is required.");
            return;
        }

        if (phone.isEmpty() || !phone.matches("[0-9+\\-\\s]{8,15}")) {
            showValidation("Enter a valid phone number (8 to 15 digits).");
            return;
        }

        if (email.isEmpty() || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showValidation("Enter a valid email address.");
            return;
        }

        if (type == null || status == null || expiryDate == null) {
            showValidation("Select membership type, status, and expiry date.");
            return;
        }

        if (editingMember == null) {
            resultMember = new Member(id, fullName, phone, email, type, status, expiryDate);
        } else {
            editingMember.setFullName(fullName);
            editingMember.setPhone(phone);
            editingMember.setEmail(email);
            editingMember.setMembershipType(type);
            editingMember.setMembershipStatus(status);
            editingMember.setExpiryDate(expiryDate);
            resultMember = editingMember;
        }

        saved = true;
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        saved = false;
        closeDialog();
    }

    public boolean isSaved() {
        return saved;
    }

    public Member getResultMember() {
        return resultMember;
    }

    private void showValidation(String message) {
        validationLabel.setText(message);
        validationLabel.setManaged(true);
        validationLabel.setVisible(true);
    }

    private void closeDialog() {
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.close();
    }
}
