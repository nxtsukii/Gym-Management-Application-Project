package com.gym.app.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Personnel {

    private final StringProperty personnelId = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty position = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty specialtyOrShift = new SimpleStringProperty();
    private final DoubleProperty payRate = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();

    public Personnel(String personnelId,
            String fullName,
            String position,
            String phone,
            String email,
            String specialtyOrShift,
            double payRate,
            String status) {
        setPersonnelId(personnelId);
        setFullName(fullName);
        setPosition(position);
        setPhone(phone);
        setEmail(email);
        setSpecialtyOrShift(specialtyOrShift);
        setPayRate(payRate);
        setStatus(status);
    }

    public String getPersonnelId() {
        return personnelId.get();
    }

    public void setPersonnelId(String personnelId) {
        this.personnelId.set(requireText(personnelId, "Personnel ID"));
    }

    public StringProperty personnelIdProperty() {
        return personnelId;
    }

    public String getFullName() {
        return fullName.get();
    }

    public void setFullName(String fullName) {
        this.fullName.set(requireText(fullName, "Full name"));
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public String getPosition() {
        return position.get();
    }

    public void setPosition(String position) {
        this.position.set(requireText(position, "Position"));
    }

    public StringProperty positionProperty() {
        return position;
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(requireText(phone, "Phone"));
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email == null ? "" : email.trim());
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getSpecialtyOrShift() {
        return specialtyOrShift.get();
    }

    public void setSpecialtyOrShift(String specialtyOrShift) {
        this.specialtyOrShift.set(specialtyOrShift == null ? "" : specialtyOrShift.trim());
    }

    public StringProperty specialtyOrShiftProperty() {
        return specialtyOrShift;
    }

    public double getPayRate() {
        return payRate.get();
    }

    public void setPayRate(double payRate) {
        if (!Double.isFinite(payRate) || payRate < 0.0) {
            throw new IllegalArgumentException("Pay rate must be a finite, non-negative amount.");
        }
        this.payRate.set(payRate);
    }

    public DoubleProperty payRateProperty() {
        return payRate;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(requireText(status, "Status"));
    }

    public StringProperty statusProperty() {
        return status;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
        return value.trim();
    }
}