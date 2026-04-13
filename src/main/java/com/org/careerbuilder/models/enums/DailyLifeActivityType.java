package com.org.careerbuilder.models.enums;

import java.util.Arrays;

public enum DailyLifeActivityType {
    SCHOOL_TIME("School Time", "#185abf"),
    STUDY_HOMEWORK("Study / Homework", "#0aac33"),
    PHYSICAL_ACTIVITY("Physical Activity", "#ffb800"),
    REST_TIME("Rest Time", "#9c27b0"),
    OTHER("Other", "#707070");

    private final String label;
    private final String color;

    DailyLifeActivityType(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public static DailyLifeActivityType fromValue(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Activity type is required");
        }

        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(normalized)
                        || type.label.equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid activity type: " + value));
    }
}