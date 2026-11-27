package com.gym.app.services;

import java.util.List;
import java.util.Optional;

import com.gym.app.models.Personnel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * In-memory stub service providing pre-populated Staff and Personal Trainer
 * records.
 * Isolates personnel data access behind a clean service boundary so a JDBC/DAO
 * implementation can replace it later without changing controllers or FXML
 * views.
 */
public class StubPersonnelService {

    private static final ObservableList<Personnel> SHARED_PERSONNEL_STORE = FXCollections.observableArrayList();

    static {
        seedInitialRecords();
    }

    /**
     * Returns the live observable list of personnel records for UI binding.
     */
    public ObservableList<Personnel> getAllPersonnel() {
        return SHARED_PERSONNEL_STORE;
    }

    /**
     * Looks up a personnel record by ID (case-insensitive).
     */
    public Optional<Personnel> findById(String personnelId) {
        if (personnelId == null || personnelId.isBlank()) {
            return Optional.empty();
        }
        String normalized = personnelId.trim();
        return SHARED_PERSONNEL_STORE.stream()
                .filter(p -> p.getPersonnelId().equalsIgnoreCase(normalized))
                .findFirst();
    }

    /**
     * Checks whether a given personnel ID already exists, excluding a specific
     * record.
     */
    public boolean isDuplicateId(String candidateId, Personnel excludedRecord) {
        if (candidateId == null || candidateId.isBlank()) {
            return false;
        }
        String normalized = candidateId.trim();
        return SHARED_PERSONNEL_STORE.stream()
                .anyMatch(p -> p != excludedRecord && p.getPersonnelId().equalsIgnoreCase(normalized));
    }

    /**
     * Resets the in-memory store back to its default 9 seeded records.
     */
    public static synchronized void resetToDefaults() {
        SHARED_PERSONNEL_STORE.clear();
        seedInitialRecords();
    }

    private static void seedInitialRecords() {
        SHARED_PERSONNEL_STORE.addAll(List.of(
                new Personnel(
                        "PER-1001",
                        "Tran Minh Quan",
                        "Staff",
                        "0901112233",
                        "quan.tran@gym.vn",
                        "Front Desk - Morning (06:00 - 14:00)",
                        75_000,
                        "Active"),
                new Personnel(
                        "PER-1002",
                        "Le Hoang Mai",
                        "Staff",
                        "0902223344",
                        "mai.le@gym.vn",
                        "Front Desk - Evening (14:00 - 22:00)",
                        75_000,
                        "Active"),
                new Personnel(
                        "PER-1003",
                        "Nguyen Gia Huy",
                        "Personal Trainer",
                        "0913334455",
                        "huy.nguyen@gym.vn",
                        "Strength & Hypertrophy",
                        180_000,
                        "Active"),
                new Personnel(
                        "PER-1004",
                        "Pham Thanh Thao",
                        "Personal Trainer",
                        "0914445566",
                        "thao.pham@gym.vn",
                        "HIIT & Weight Loss",
                        170_000,
                        "Active"),
                new Personnel(
                        "PER-1005",
                        "Vo Duc Anh",
                        "Personal Trainer",
                        "0925556677",
                        "anh.vo@gym.vn",
                        "Calisthenics & Mobility",
                        165_000,
                        "Active"),
                new Personnel(
                        "PER-1006",
                        "Dang Thu Trang",
                        "Staff",
                        "0936667788",
                        "trang.dang@gym.vn",
                        "Membership & Billing Support",
                        80_000,
                        "On Leave"),
                new Personnel(
                        "PER-1007",
                        "Bui Quoc Khanh",
                        "Personal Trainer",
                        "0947778899",
                        "khanh.bui@gym.vn",
                        "Athletic Conditioning",
                        190_000,
                        "Active"),
                new Personnel(
                        "PER-1008",
                        "Hoang Kim Ngan",
                        "Personal Trainer",
                        "0958889900",
                        "ngan.hoang@gym.vn",
                        "Post-Rehab & Corrective Exercise",
                        200_000,
                        "On Leave"),
                new Personnel(
                        "PER-1009",
                        "Doan Van Tuan",
                        "Staff",
                        "0969990011",
                        "tuan.doan@gym.vn",
                        "Facility & Locker Operations",
                        70_000,
                        "Inactive")));
    }
}