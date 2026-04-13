package com.org.careerbuilder.models.enums;

public enum CareerAssessmentType {

    PERSONALITY_PROFILE("Personality Profile", "Pathway Planners", "#FFF4E5"),
    APTITUDE_ASSESSMENT("Aptitude Assessment", "Pathway Planners", "#E8F5E9"),
    MULTIPLE_INTELLIGENCE("Multiple Intelligence", "Pathway Planners", "#E3F2FD"),
    COMPREHENSIVE_CAREER("Comprehensive Career", "Pathway Planners", "#F3E5F5");

    private final String label;
    private final String category;
    private final String bgColor;

    CareerAssessmentType(String label, String category, String bgColor) {
        this.label = label;
        this.category = category;
        this.bgColor = bgColor;
    }

    public String getLabel() { return label; }
    public String getCategory() { return category; }
    public String getBgColor() { return bgColor; }

    public static CareerAssessmentType fromValue(String value) {
        if (value == null) return PERSONALITY_PROFILE;
        for (CareerAssessmentType t : values()) {
            if (t.name().equalsIgnoreCase(value) || t.label.equalsIgnoreCase(value)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Unknown assessment type: " + value);
    }
}
