package com.gym.app.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CheckInRecord {

    private final StringProperty memberId = new SimpleStringProperty();
    private final StringProperty memberName = new SimpleStringProperty();
    private final StringProperty membershipType = new SimpleStringProperty();
    private final StringProperty checkInTime = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public CheckInRecord(String memberId,
                         String memberName,
                         String membershipType,
                         String checkInTime,
                         String status) {
        this.memberId.set(memberId);
        this.memberName.set(memberName);
        this.membershipType.set(membershipType);
        this.checkInTime.set(checkInTime);
        this.status.set(status);
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

    public String getMemberName() {
        return memberName.get();
    }

    public void setMemberName(String memberName) {
        this.memberName.set(memberName);
    }

    public StringProperty memberNameProperty() {
        return memberName;
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

    public String getCheckInTime() {
        return checkInTime.get();
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime.set(checkInTime);
    }

    public StringProperty checkInTimeProperty() {
        return checkInTime;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }
}
