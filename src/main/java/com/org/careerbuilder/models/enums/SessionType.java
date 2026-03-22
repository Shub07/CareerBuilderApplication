package com.org.careerbuilder.models.enums;

/**
 * Defines the type of a class session.
 *
 * This enum is used to:
 * - Decide whether a session is a real teaching class or not
 * - Calculate total teaching hours vs break hours
 * - Control UI rendering (class card vs break row)
 *
 * Stored in DB as STRING (REGULAR, BREAK, etc.)
 */
public enum SessionType {

    /**
     * Normal subject class
     * Example: Maths, English, Science
     */
    REGULAR,

    /**
     * Extra or special session
     * Example: Revision class, Guest lecture, Exam preparation
     */
    SPECIAL,

    /**
     * Short break between classes
     * Example: Tea break
     */
    BREAK,

    /**
     * Long break
     * Example: Lunch break
     */
    LUNCH
}
