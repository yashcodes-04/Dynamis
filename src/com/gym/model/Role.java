package com.gym.model;

public enum Role {
    MEMBER("Gym Member", "Access workouts, classes, and personal membership"),
    RECEPTIONIST("Receptionist", "Manage check-ins, memberships, and inquiries"),
    TRAINER("Fitness Trainer", "Manage clients, workout routines, and sessions"),
    OWNER("Gym Owner", "Overview analytics, staff management, and finances");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return MEMBER;
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(roleStr) || r.getDisplayName().equalsIgnoreCase(roleStr)) {
                return r;
            }
        }
        return MEMBER;
    }
}
