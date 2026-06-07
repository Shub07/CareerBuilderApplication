package com.org.careerbuilder.models.enums;

/**
 * Role a teacher holds for a class-section-subject allocation.
 * Mirrors the "Role" dropdown in the Allocate Class &amp; Subject modal.
 */
public enum ClassAssignmentRole {
    CLASS_TEACHER("Class Teacher"),
    SUBJECT_TEACHER("Subject Teacher"),
    ASSISTANT_TEACHER("Assistant Teacher");

    private final String label;

    ClassAssignmentRole(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ClassAssignmentRole fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            return SUBJECT_TEACHER;
        }
        String normalized = raw.trim().toUpperCase().replace(' ', '_');
        for (ClassAssignmentRole r : values()) {
            if (r.name().equals(normalized) || r.label.equalsIgnoreCase(raw.trim())) {
                return r;
            }
        }
        return SUBJECT_TEACHER;
    }
}
