package com.gym.app.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class Member {

    private final StringProperty memberId = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty membershipType = new SimpleStringProperty();
    private final StringProperty membershipStatus = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> expiryDate = new SimpleObjectProperty<>();

    public Member(String memberId,
                  String fullName,
                  String phone,
                  String email,
                  String membershipType,
                  String membershipStatus,
                  LocalDate expiryDate) {
        this.memberId.set(memberId);
        this.fullName.set(fullName);
        this.phone.set(phone);
        this.email.set(email);
        this.membershipType.set(membershipType);
        this.membershipStatus.set(membershipStatus);
        this.expiryDate.set(expiryDate);
    }

    public String getMemberId() {
        return memberId.get();
    }

    public void setMemberId(String memberId) {
        this.memberId.set(memberId);
    }

    public StringProperty memberIdProperty() {
        return memberId;
    }

    public String getFullName() {
        return fullName.get();
    }

    public void setFullName(String fullName) {
        this.fullName.set(fullName);
    }

    public StringProperty fullNameProperty() {
        return fullName;
    }

    public String getPhone() {
        return phone.get();
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public StringProperty emailProperty() {
        return email;
    }

    public String getMembershipType() {
        return membershipType.get();
    }

    public void setMembershipType(String membershipType) {
        this.membershipType.set(membershipType);
    }

    public StringProperty membershipTypeProperty() {
        return membershipType;
    }

    public String getMembershipStatus() {
        return membershipStatus.get();
    }

    public void setMembershipStatus(String membershipStatus) {
        this.membershipStatus.set(membershipStatus);
    }

    public StringProperty membershipStatusProperty() {
        return membershipStatus;
    }

    public LocalDate getExpiryDate() {
        return expiryDate.get();
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate.set(expiryDate);
    }

    public ObjectProperty<LocalDate> expiryDateProperty() {
        return expiryDate;
    }
}
