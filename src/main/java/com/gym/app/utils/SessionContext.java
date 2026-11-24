package com.gym.app.utils;

import java.util.Objects;

public final class SessionContext {
    public enum Role {
        OWNER("Gym Owner"),
        STAFF("Gym Staff");

        private final String displayLabel;

        Role(String displayLabel) {
            this.displayLabel = displayLabel;
        }

        public String getDisplayLabel() {
            return displayLabel;
        }
    }

    private static volatile String currentUserId;
    private static volatile String currentDisplayName;
    private static volatile Role currentRole;

    private SessionContext() {
        // Prevent instantiation of static session holder
    }

    // Initializes or replaces the active user session upon successful login.
    public static synchronized void startSession(String userId, String displayName, Role role) {
        // Unique user identifier
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID must not be null or blank.");
        }
        // Human-readable name shown in the workspace UI
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name must not be null or blank.");
        }
        // Assigned authorization role (OWNER or STAFF)
        currentRole = Objects.requireNonNull(role, "Role must not be null.");
        currentUserId = userId.trim();
        currentDisplayName = displayName.trim();
    }

    // Clears all session attributes upon logout or session termination
    public static synchronized void clear() {
        currentUserId = null;
        currentDisplayName = null;
        currentRole = null;
    }

    public static boolean isAuthenticated() {
        return currentUserId != null && currentRole != null;
    }

    public static String getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentDisplayName() {
        return currentDisplayName;
    }

    public static Role getCurrentRole() {
        return currentRole;
    }

    public static boolean isOwner() {
        return currentRole == Role.OWNER;
    }

    public static boolean hasRole(Role requiredRole) {
        if (requiredRole == null || currentRole == null) {
            return false;
        }
        // OWNER generalizes STAFF and therefore satisfies STAFF-level checks
        return currentRole == requiredRole || currentRole == Role.OWNER;
    }
}