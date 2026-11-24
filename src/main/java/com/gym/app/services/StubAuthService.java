package com.gym.app.services;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.gym.app.utils.SessionContext.Role;

/**
 * In-memory authentication stub for frontend development and role testing.
 * Isolates credential verification behind a clean service boundary so a
 * database-backed
 * implementation can replace it later without modifying controllers or FXML
 * views.
 */
public class StubAuthService {

    // Safe user projection returned to the presentation layer after authentication
    public record AuthenticatedUser(
            String userId,
            String username,
            String displayName,
            Role role) {
    }

    // Preset credential pair used by one-click demo buttons in the login screen
    public record TestCredential(
            String username,
            String password,
            String displayName,
            Role role) {
    }

    private record StubAccount(
            String userId,
            String username,
            String password,
            String displayName,
            Role role) {
    }

    private final Map<String, StubAccount> accountsByUsername = new LinkedHashMap<>();

    public StubAuthService() {
        registerAccount(new StubAccount(
                "USR-001",
                "owner",
                "owner123",
                "Tran Minh Quan",
                Role.OWNER));
        registerAccount(new StubAccount(
                "USR-002",
                "staff",
                "staff123",
                "Le Hoang Mai",
                Role.STAFF));
    }

    private void registerAccount(StubAccount account) {
        accountsByUsername.put(account.username().toLowerCase(Locale.ROOT), account);
    }

    // Authenticates a username and password against registered stub accounts
    // Username matching is case-insensitive; password matching is exact
    public Optional<AuthenticatedUser> authenticate(String username, String password) {
        if (username == null || password == null) {
            return Optional.empty();
        }

        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        if (normalizedUsername.isEmpty() || password.isEmpty()) {
            return Optional.empty();
        }

        StubAccount account = accountsByUsername.get(normalizedUsername);
        if (account == null || !account.password().equals(password)) {
            return Optional.empty();
        }

        return Optional.of(new AuthenticatedUser(
                account.userId(),
                account.username(),
                account.displayName(),
                account.role()));
    }

    // Retrieves one-click demo credentials for the requested role.
    public TestCredential getTestCredentialForRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null.");
        return accountsByUsername.values().stream()
                .filter(acc -> acc.role() == role)
                .findFirst()
                .map(acc -> new TestCredential(
                        acc.username(),
                        acc.password(),
                        acc.displayName(),
                        acc.role()))
                .orElseThrow(() -> new IllegalStateException("No stub credential configured for role: " + role));
    }
}