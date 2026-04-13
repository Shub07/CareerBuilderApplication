package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Batch upload tracking for attendance records
 */
@Entity
@Table(
    name = "attendance_batch_uploads",
    indexes = {
        @Index(name = "idx_batch_school_date", columnList = "school_id, upload_date"),
        @Index(name = "idx_batch_status", columnList = "upload_status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_batch_upload", columnNames = {"school_id", "class_name", "section", "upload_date"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AttendanceBatchUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_id")
    private Long id;

    @NotNull
    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @NotNull
    @Column(name = "class_name", nullable = false)
    private String className;

    @NotNull
    @Column(name = "section", nullable = false)
    private String section;

    @NotNull
    @Column(name = "upload_date", nullable = false)
    private LocalDate uploadDate;

    @NotNull
    @Column(name = "uploaded_by_id", nullable = false)
    private Long uploadedById;

    @Column(name = "total_records", nullable = false)
    private Integer totalRecords = 0;

    @Column(name = "successful_records", nullable = false)
    private Integer successfulRecords = 0;

    @Column(name = "failed_records", nullable = false)
    private Integer failedRecords = 0;

    @Column(name = "upload_status", nullable = false, length = 50)
    private String uploadStatus = "PENDING"; // PENDING, COMPLETED, FAILED

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

