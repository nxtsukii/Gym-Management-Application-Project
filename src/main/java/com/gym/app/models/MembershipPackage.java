package com.gym.app.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MembershipPackage {

    private final StringProperty packageId = new SimpleStringProperty();
    private final StringProperty packageName = new SimpleStringProperty();
    private final IntegerProperty durationMonths = new SimpleIntegerProperty();
    private final DoubleProperty price = new SimpleDoubleProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final BooleanProperty active = new SimpleBooleanProperty();

    public MembershipPackage(String packageId,
                             String packageName,
                             int durationMonths,
                             double price,
                             String description,
                             boolean active) {
        setPackageId(packageId);
        setPackageName(packageName);
        setDurationMonths(durationMonths);
        setPrice(price);
        setDescription(description);
        setActive(active);
    }

    public String getPackageId() {
        return packageId.get();
    }

    public void setPackageId(String packageId) {
        this.packageId.set(requireText(packageId, "Package ID"));
    }

    public StringProperty packageIdProperty() {
        return packageId;
    }

    public String getPackageName() {
        return packageName.get();
    }

    public void setPackageName(String packageName) {
        this.packageName.set(requireText(packageName, "Package name"));
    }

    public StringProperty packageNameProperty() {
        return packageName;
    }

    public int getDurationMonths() {
        return durationMonths.get();
    }

    public void setDurationMonths(int durationMonths) {
        if (durationMonths <= 0) {
            throw new IllegalArgumentException("Duration in months must be greater than zero.");
        }
        this.durationMonths.set(durationMonths);
    }

    public IntegerProperty durationMonthsProperty() {
        return durationMonths;
    }

    public double getPrice() {
        return price.get();
    }

    public void setPrice(double price) {
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("Price must be a finite, non-negative amount.");
        }
        this.price.set(price);
    }

    public DoubleProperty priceProperty() {
        return price;
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean active) {
        this.active.set(active);
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public String getFormattedDuration() {
        return durationMonths.get() + (durationMonths.get() == 1 ? " month" : " months");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
        }
        return value;
    }
}
