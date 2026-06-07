package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "exam_venue_allocations",
        uniqueConstraints = @UniqueConstraint(name = "uk_exam_venue_allocation", columnNames = {"exam_id", "venue_id"}),
        indexes = @Index(name = "idx_exam_venue_alloc_exam", columnList = "exam_id")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamVenueAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private ExamVenue venue;

    @Column(name = "capacity_limit")
    private Integer capacityLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private Faculty administrator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_invigilator_id")
    private Faculty primaryInvigilator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assistant_invigilator_id")
    private Faculty assistantInvigilator;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public int effectiveCapacity() {
        if (capacityLimit != null && capacityLimit > 0) {
            return capacityLimit;
        }
        return venue != null ? venue.getCapacity() : 0;
    }
}
