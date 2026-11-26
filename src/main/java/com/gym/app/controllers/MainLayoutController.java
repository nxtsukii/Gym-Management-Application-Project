package com.gym.app.controllers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gym.app.models.CheckInRecord;
import com.gym.app.models.Member;
import com.gym.app.models.MembershipPackage;
import com.gym.app.utils.AlertUtil;
import com.gym.app.utils.SessionContext;
import com.gym.app.utils.SessionContext.Role;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainLayoutController {

    private static final Logger LOGGER = Logger.getLogger(MainLayoutController.class.getName());
    private static final String LOGIN_VIEW_FXML = "/views/common/LoginView.fxml";
    private static final String APP_CSS = "/css/app.css";

    public enum AppRoute {
        DASHBOARD(
                "Dashboard & Reports",
                "Gym KPIs, membership summary, and operational alerts",
                "/views/admin/DashboardView.fxml",
                Role.OWNER,
                "Dashboard & Reports placeholder (Use Case: View Dashboard and Alerts)."),
        MEMBERS(
                "Members",
                "View, search, register, edit, and manage gym members",
                "/views/frontdesk/MemberManagementView.fxml",
                Role.STAFF,
                "Member Management view could not be loaded."),
        MEMBERSHIPS(
                "Memberships",
                "Manage subscription packages, renewals, and expiration rules",
                "/views/frontdesk/MembershipManagementView.fxml",
                Role.STAFF,
                "Membership Management placeholder (Use Case: Manage Memberships)."),
        PAYMENTS(
                "Payments",
                "Record membership fees, inspect receipts, and track payment history",
                "/views/frontdesk/PaymentManagementView.fxml",
                Role.STAFF,
                "Payments placeholder (Use Case: Manage Payments)."),
        CHECK_IN(
                "Check-in",
                "Search members, verify membership eligibility, and record check-ins",
                "/views/frontdesk/CheckInView.fxml",
                Role.STAFF,
                "Member Check-in view could not be loaded."),
        PERSONNEL(
                "Personnel",
                "Manage staff profiles, trainer schedules, and system access roles",
                "/views/admin/PersonnelView.fxml",
                Role.OWNER,
                "Personnel Records placeholder (Use Case: Manage Personnel Records)."),
        EQUIPMENT(
                "Equipment",
                "Track gym machines, maintenance logs, and operational condition",
                "/views/admin/EquipmentView.fxml",
                Role.OWNER,
                "Equipment Management placeholder (Use Case: Manage Equipment).");

        private final String title;
        private final String subtitle;
        private final String fxmlPath;
        private final Role requiredRole;
        private final String placeholderMessage;

        AppRoute(String title,
                String subtitle,
                String fxmlPath,
                Role requiredRole,
                String placeholderMessage) {
            this.title = title;
            this.subtitle = subtitle;
            this.fxmlPath = fxmlPath;
            this.requiredRole = requiredRole;
            this.placeholderMessage = placeholderMessage;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getFxmlPath() {
            return fxmlPath;
        }

        public Role getRequiredRole() {
            return requiredRole;
        }

        public String getPlaceholderMessage() {
            return placeholderMessage;
        }
    }

    // Sidebar Navigation Controls
    @FXML
    private Button dashboardBtn;

    @FXML
    private Label operationsSectionLabel;

    @FXML
    private Button membersBtn;

    @FXML
    private Button membershipsBtn;

    @FXML
    private Button paymentsBtn;

    @FXML
    private Button checkInBtn;

    @FXML
    private Label adminSectionLabel;

    @FXML
    private Button personnelBtn;

    @FXML
    private Button equipmentBtn;

    @FXML
    private Label sidebarScopeLabel;

    // Top Header Bar Controls
    @FXML
    private Label headerTitleLabel;

    @FXML
    private Label headerSubtitleLabel;

    @FXML
    private Label userInitialsLabel;

    @FXML
    private Label userBadgeLabel;

    @FXML
    private Label userIdLabel;

    @FXML
    private Label roleChipLabel;

    @FXML
    private Button logoutButton;

    // Dynamic Center Content Area
    @FXML
    private StackPane contentArea;

    private final Map<AppRoute, Button> routeButtons = new EnumMap<>(AppRoute.class);
    private final ObservableList<Member> members = FXCollections.observableArrayList();
    private final ObservableList<CheckInRecord> checkInRecords = FXCollections.observableArrayList();
    private final ObservableList<MembershipPackage> membershipPackages = FXCollections.observableArrayList();

    private AppRoute currentRoute;
    private boolean loggingOut;

    @FXML
    public void initialize() {
        // Guard against direct FXML loading without an authenticated SessionContext
        if (!SessionContext.isAuthenticated()) {
            LOGGER.warning("Unauthenticated access attempt to MainLayout; redirecting to LoginView.");
            Platform.runLater(this::performLogoutRedirect);
            return;
        }

        registerRouteButtons();
        bindUserIdentityHeader();
        applyRoleBasedNavigation();
        loadSampleData();

        // Land on default workspace route based on authenticated role
        AppRoute defaultRoute = SessionContext.isOwner() ? AppRoute.DASHBOARD : AppRoute.MEMBERS;
        navigateTo(defaultRoute);
    }

    private void registerRouteButtons() {
        routeButtons.put(AppRoute.DASHBOARD, dashboardBtn);
        routeButtons.put(AppRoute.MEMBERS, membersBtn);
        routeButtons.put(AppRoute.MEMBERSHIPS, membershipsBtn);
        routeButtons.put(AppRoute.PAYMENTS, paymentsBtn);
        routeButtons.put(AppRoute.CHECK_IN, checkInBtn);
        routeButtons.put(AppRoute.PERSONNEL, personnelBtn);
        routeButtons.put(AppRoute.EQUIPMENT, equipmentBtn);
    }

    private void bindUserIdentityHeader() {
        String displayName = SessionContext.getCurrentDisplayName();
        String userId = SessionContext.getCurrentUserId();
        Role role = SessionContext.getCurrentRole();

        userBadgeLabel.setText(displayName != null ? displayName : "Unknown User");
        userIdLabel.setText(userId != null ? userId : "-");
        userInitialsLabel.setText(extractInitials(displayName));

        if (role != null) {
            roleChipLabel.setText(role.getDisplayLabel());
            roleChipLabel.getStyleClass().setAll(
                    role == Role.OWNER ? "role-chip-owner" : "role-chip-staff");
            sidebarScopeLabel.setText(
                    role == Role.OWNER ? "Full Owner Access (7 Routes)" : "Operational Access (4 Routes)");
        }
    }

    private void applyRoleBasedNavigation() {
        boolean isOwner = SessionContext.isOwner();

        // Owner-only sidebar controls
        setNodeVisibleAndManaged(dashboardBtn, isOwner);
        setNodeVisibleAndManaged(adminSectionLabel, isOwner);
        setNodeVisibleAndManaged(personnelBtn, isOwner);
        setNodeVisibleAndManaged(equipmentBtn, isOwner);

        // Shared operational sidebar controls (accessible to STAFF and inherited by
        // OWNER)
        setNodeVisibleAndManaged(operationsSectionLabel, true);
        setNodeVisibleAndManaged(membersBtn, true);
        setNodeVisibleAndManaged(membershipsBtn, true);
        setNodeVisibleAndManaged(paymentsBtn, true);
        setNodeVisibleAndManaged(checkInBtn, true);
    }

    private void setNodeVisibleAndManaged(Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    // Sidebar Navigation Event Handlers
    @FXML
    private void handleShowDashboard() {
        navigateTo(AppRoute.DASHBOARD);
    }

    @FXML
    private void handleShowMembers() {
        navigateTo(AppRoute.MEMBERS);
    }

    @FXML
    private void handleShowMemberships() {
        navigateTo(AppRoute.MEMBERSHIPS);
    }

    @FXML
    private void handleShowPayments() {
        navigateTo(AppRoute.PAYMENTS);
    }

    @FXML
    private void handleShowCheckIn() {
        navigateTo(AppRoute.CHECK_IN);
    }

    @FXML
    private void handleShowPersonnel() {
        navigateTo(AppRoute.PERSONNEL);
    }

    @FXML
    private void handleShowEquipment() {
        navigateTo(AppRoute.EQUIPMENT);
    }

    // Programmatic Route Guard & Dynamic Content Loader
    /**
     * Central view-switching entry point with programmatic RBAC route guards.
     * Blocks unauthorized navigation even if triggered internally outside sidebar
     * clicks.
     *
     * @param targetRoute destination workspace route
     * @return true if navigation succeeded; false if blocked by session/role guard
     */
    public boolean navigateTo(AppRoute targetRoute) {
        Objects.requireNonNull(targetRoute, "Target route must not be null.");

        // Session integrity guard
        if (!SessionContext.isAuthenticated()) {
            LOGGER.warning("Blocked navigation to " + targetRoute + ": no active session.");
            performLogoutRedirect();
            return false;
        }

        // Programmatic Role-Based Access Control guard (enforces Use Case inheritance)
        if (!SessionContext.hasRole(targetRoute.getRequiredRole())) {
            Role activeRole = SessionContext.getCurrentRole();
            LOGGER.warning(() -> String.format(
                    "RBAC Route Guard blocked user '%s' (role=%s) from accessing '%s' (requires=%s).",
                    SessionContext.getCurrentUserId(),
                    activeRole,
                    targetRoute.name(),
                    targetRoute.getRequiredRole()));

            AlertUtil.showWarning(
                    resolveWindow(),
                    "Access Restricted",
                    "Permission Denied",
                    "Your current role (" + activeRole.getDisplayLabel()
                            + ") is not authorized to access " + targetRoute.getTitle() + ".");
            return false;
        }

        // Skip redundant reload if already viewing the requested route
        if (targetRoute == currentRoute && !contentArea.getChildren().isEmpty()) {
            return true;
        }

        currentRoute = targetRoute;
        updateActiveNavButton(targetRoute);
        headerTitleLabel.setText(targetRoute.getTitle());
        headerSubtitleLabel.setText(targetRoute.getSubtitle());

        loadContentOrPlaceholder(targetRoute);
        return true;
    }

    private void updateActiveNavButton(AppRoute activeRoute) {
        for (Map.Entry<AppRoute, Button> entry : routeButtons.entrySet()) {
            Button button = entry.getValue();
            if (button != null) {
                boolean isActive = entry.getKey() == activeRoute;
                button.getStyleClass().setAll(isActive ? "nav-button-active" : "nav-button");
            }
        }
    }

    private void loadContentOrPlaceholder(AppRoute route) {
        URL resource = getClass().getResource(route.getFxmlPath());
        if (resource == null) {
            contentArea.getChildren().setAll(createPlaceholderCard(route));
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Node view = loader.load();

            // Inject shared front-desk state into active sub-controllers
            Object controller = loader.getController();
            if (controller instanceof MemberManagementController memberController) {
                memberController.setSharedData(members, membershipPackages);
            } else if (controller instanceof MembershipManagementController membershipController) {
                membershipController.setMembershipPackages(membershipPackages);
            } else if (controller instanceof CheckInController checkInController) {
                checkInController.setSharedData(members, checkInRecords);
            }

            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML for route: " + route.name(), e);
            contentArea.getChildren().setAll(
                    createErrorCard("Unable to Load Screen",
                            "An error occurred while loading " + route.getTitle() + ": " + e.getMessage()));
        }
    }

    private VBox createPlaceholderCard(AppRoute route) {
        Label scopeChip = new Label(
                route.getRequiredRole() == Role.OWNER ? "OWNER-ONLY MODULE" : "OPERATIONAL MODULE");
        scopeChip.getStyleClass().add(
                route.getRequiredRole() == Role.OWNER ? "role-chip-owner" : "role-chip-staff");

        Label titleLabel = new Label(route.getTitle());
        titleLabel.getStyleClass().add("card-title");

        Label messageLabel = new Label(route.getPlaceholderMessage());
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("card-subtitle");

        VBox card = new VBox(10.0, scopeChip, titleLabel, messageLabel);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(520.0);
        card.setMaxHeight(200.0);
        card.getStyleClass().add("card");
        return card;
    }

    private VBox createErrorCard(String title, String message) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("card-subtitle");

        VBox card = new VBox(8.0, titleLabel, messageLabel);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(520.0);
        card.setMaxHeight(180.0);
        card.getStyleClass().add("card");
        return card;
    }

    // Logout
    @FXML
    private void handleLogout() {
        if (loggingOut) {
            return;
        }
        loggingOut = true;
        if (logoutButton != null) {
            logoutButton.setDisable(true);
        }

        // Clear session state and in-memory workspace data before swapping scene
        SessionContext.clear();
        currentRoute = null;
        members.clear();
        checkInRecords.clear();
        membershipPackages.clear();

        performLogoutRedirect();
    }

    private void performLogoutRedirect() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Objects.requireNonNull(getClass().getResource(LOGIN_VIEW_FXML),
                            "LoginView.fxml was not found"));
            Parent root = loader.load();

            Stage stage = resolveStage();
            if (stage == null) {
                return;
            }

            Scene loginScene = new Scene(root, 900, 620);
            loginScene.getStylesheets().add(Objects.requireNonNull(
                    getClass().getResource(APP_CSS),
                    "app.css was not found").toExternalForm());

            stage.setMinWidth(520);
            stage.setMinHeight(500);
            stage.setWidth(900);
            stage.setHeight(620);
            stage.setScene(loginScene);
            stage.centerOnScreen();
        } catch (IOException | RuntimeException e) {
            loggingOut = false;
            if (logoutButton != null) {
                logoutButton.setDisable(false);
            }
            LOGGER.log(Level.SEVERE, "Failed to return to LoginView.fxml during logout.", e);
            AlertUtil.showError(
                    resolveWindow(),
                    "Logout Error",
                    "Unable to return to the login screen: " + e.getMessage());
        }
    }

    // Helpers & Shared Demo Data
    private void loadSampleData() {
        members.setAll(
                new Member("MEM-1001", "Nguyen Minh Anh", "0901234567", "anh.nguyen@email.com",
                        "Annual", "Active", LocalDate.now().plusMonths(8)),
                new Member("MEM-1002", "Tran Quoc Bao", "0912345678", "bao.tran@email.com",
                        "Monthly", "Active", LocalDate.now().plusDays(18)),
                new Member("MEM-1003", "Le Thu Ha", "0923456789", "ha.le@email.com",
                        "Quarterly", "Expired", LocalDate.now().minusDays(12)),
                new Member("MEM-1004", "Pham Duc Huy", "0934567890", "huy.pham@email.com",
                        "VIP", "Active", LocalDate.now().plusMonths(5)),
                new Member("MEM-1005", "Vo Khanh Linh", "0945678901", "linh.vo@email.com",
                        "Monthly", "Inactive", LocalDate.now().minusDays(45)),
                new Member("MEM-1006", "Do Hoang Nam", "0956789012", "nam.do@email.com",
                        "Quarterly", "Active", LocalDate.now().plusMonths(2)));

        membershipPackages.setAll(
                new MembershipPackage("PKG-001", "Monthly", 1, 300_000,
                        "Monthly", true),
                new MembershipPackage("PKG-002", "Quarterly", 3, 810_000,
                        "Quarterly", true),
                new MembershipPackage("PKG-003", "Annual", 12, 2_880_000,
                        "Annual", true),
                new MembershipPackage("PKG-004", "VIP", 12, 5_000_000,
                        "VIP", true));

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        checkInRecords.setAll(
                new CheckInRecord("MEM-1001", "Nguyen Minh Anh", "Annual", today + " 07:45:00", "Checked In"),
                new CheckInRecord("MEM-1004", "Pham Duc Huy", "VIP", today + " 08:20:00", "Checked In"),
                new CheckInRecord("MEM-1002", "Tran Quoc Bao", "Monthly", today + " 09:05:00", "Checked In"));
    }

    private String extractInitials(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return "U";
        }
        String[] parts = displayName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase(Locale.ROOT);
        }
        String first = parts[0].substring(0, 1);
        String last = parts[parts.length - 1].substring(0, 1);
        return (first + last).toUpperCase(Locale.ROOT);
    }

    private Stage resolveStage() {
        Window window = resolveWindow();
        return window instanceof Stage stage ? stage : null;
    }

    private Window resolveWindow() {
        return contentArea != null && contentArea.getScene() != null
                ? contentArea.getScene().getWindow()
                : null;
    }

    public ObservableList<Member> getMembers() {
        return members;
    }

    public ObservableList<CheckInRecord> getCheckInRecords() {
        return checkInRecords;
    }
}