package com.org.careerbuilder.models.enums;

/**
 * Represents the execution status of a class session.
 *
 * This enum is used to:
 * - Decide whether "Join Class" button should be shown
 * - Calculate completed vs pending classes
 * - Compute course progress percentage
 */
public enum SessionStatus {

    /**
     * Session is planned but not yet completed
     * UI should show: "Join Class"
     */
    SCHEDULED,

    /**
     * Session has already happened
     * UI should show: "Class Over"
     */
    COMPLETED,

    /**
     * Session was cancelled
     * Should NOT be counted in progress or hours
     */
    CANCELLED
}
