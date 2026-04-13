package com.org.careerbuilder.models.enums;

/**
 * 📢 Notice Category Enum
 * Defines different types/categories of notices
 */
public enum NoticeCategory {
    ACADEMIC("Academic", "#185abf", "#f2f5ff"),
    EXAMS("Exams", "#ff3a3a", "#ffebee"),
    EVENTS("Events", "#ffb800", "#fff4e5"),
    HOLIDAYS("Holidays", "#9c27b0", "#f5e6ff"),
    GENERAL("General", "#0aac33", "#e8f5e9"),
    OTHER("Other", "#757575", "#f5f5f5");

    private final String label;
    private final String color;
    private final String background;

    NoticeCategory(String label, String color, String background) {
        this.label = label;
        this.color = color;
        this.background = background;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }

    public String getBackground() {
        return background;
    }

    /**
     * Convert string to NoticeCategory enum
     */
    public static NoticeCategory fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return OTHER;
        }
        
        for (NoticeCategory category : NoticeCategory.values()) {
            if (category.name().equalsIgnoreCase(value) || category.label.equalsIgnoreCase(value)) {
                return category;
            }
        }
        return OTHER;
    }
}
