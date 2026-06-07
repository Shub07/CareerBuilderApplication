package com.org.careerbuilder.models.enums;

/**
 * Lifecycle status of an academic class shown in the Class Details list.
 * A class becomes ACTIVE once it has a class teacher and at least one section;
 * otherwise it is typically INACTIVE / unassigned.
 */
public enum ClassStatus {
    ACTIVE,
    INACTIVE;

    public static ClassStatus fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return ACTIVE;
        }
        return ACTIVE.name().equalsIgnoreCase(raw.trim()) ? ACTIVE : INACTIVE;
    }
}
