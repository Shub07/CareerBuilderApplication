package com.org.careerbuilder.models.enums;

/**
 * Billing cadence for a fee structure template.
 */
public enum FeeFrequency {
    ONE_TIME("One Time"),
    MONTHLY("Monthly"),
    QUARTERLY("Quarterly"),
    HALF_YEARLY("Half Yearly"),
    YEARLY("Yearly");

    private final String label;

    FeeFrequency(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
