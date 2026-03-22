package com.org.careerbuilder.models.enums;

/**
 * Defines the type of study material.
 *
 * This enum is used to:
 * - Display correct icon in UI (PDF, Video, etc.)
 * - Apply validations while uploading material
 * - Support future extensions like VIDEO streaming
 */
public enum MaterialType {

    /**
     * PDF documents
     * Example: Notes, Worksheets
     */
    PDF,

    /**
     * Word documents
     * Example: Assignments
     */
    DOC,

    /**
     * Video materials
     * Example: Recorded lectures
     */
    VIDEO,

    /**
     * Any external reference link
     */
    LINK
}
