package com.org.careerbuilder.models.enums;

public enum MarksSubmissionStatus {
    PENDING,
    SUBMITTED,
    APPROVED,
    REJECTED;

    public String getLabel() {
        return switch (this) {
            case PENDING -> "Pending";
            case SUBMITTED -> "Submitted";
            case APPROVED -> "Approved";
            case REJECTED -> "Rejected";
        };
    }
}
