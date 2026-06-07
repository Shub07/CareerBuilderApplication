package com.org.careerbuilder.models.enums;

public enum ExamStatus {
    DRAFT,
    SCHEDULED,
    ACTIVE,
    COMPLETED;

    public String getLabel() {
        return switch (this) {
            case DRAFT -> "Draft";
            case SCHEDULED -> "Scheduled";
            case ACTIVE -> "Active";
            case COMPLETED -> "Completed";
        };
    }
}
