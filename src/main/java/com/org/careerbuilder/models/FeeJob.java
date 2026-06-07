package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.FeeJobStatus;
import com.org.careerbuilder.models.enums.FeeJobType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks a background job for the Student Fees module (async obligation
 * generation or report export) so the UI can poll for status and download the
 * produced artifact.
 */
@Entity
@Table(name = "fee_jobs", indexes = {
        @Index(name = "idx_fee_jobs_school_status", columnList = "school_id,status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 40)
    private FeeJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FeeJobStatus status;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "processed_items", nullable = false)
    private Integer processedItems;

    @Column(name = "message", length = 500)
    private String message;

    @Column(name = "result_filename", length = 200)
    private String resultFilename;

    @Column(name = "result_content_type", length = 120)
    private String resultContentType;

    @Lob
    @Column(name = "result_data")
    private byte[] resultData;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = FeeJobStatus.QUEUED;
        }
        if (processedItems == null) {
            processedItems = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
