package com.org.careerbuilder.models.enums;

public enum AssignmentStatus {

    PENDING,
    SUBMITTED,
    LATE,
    GRADED,
    /** Teacher returned work for revision; student may resubmit if allowed. */
    RETURNED
}