package com.org.careerbuilder.models.enums;

public enum ExamRegistrationStatus {
    PENDING,
    VERIFIED;

    public String getLabel() {
        return switch (this) {
            case PENDING -> "Pending";
            case VERIFIED -> "Verified";
        };
    }
}
