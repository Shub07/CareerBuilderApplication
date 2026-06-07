package com.org.careerbuilder.models.enums;

public enum ExamType {
    ASSESSMENT,
    UNIT_TEST,
    INTERNAL,
    WEEKLY,
    MID_TERM,
    FINAL;

    public String getLabel() {
        return switch (this) {
            case ASSESSMENT -> "Assessment";
            case UNIT_TEST -> "Unit Test";
            case INTERNAL -> "Internal";
            case WEEKLY -> "Weekly Test";
            case MID_TERM -> "Mid Term";
            case FINAL -> "Final";
        };
    }
}