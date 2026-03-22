package com.org.careerbuilder.models.enums;

/**
 * Represents what action a student can take for a class session.
 *
 * This enum is NOT stored in DB.
 * It is calculated in service layer and sent to UI.
 */
public enum ClassJoinStatus {

    /**
     * Class is upcoming or ongoing
     * UI should show "Join Class"
     */
    JOIN,

    /**
     * Class has already completed
     * UI should show "Class Over"
     */
    OVER
}
