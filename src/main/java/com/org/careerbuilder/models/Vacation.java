package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 🎫 Vacation Model - School/Class Vacations Management
 * Admin creates vacations that are visible to all students
 */
@Entity
@Table(name = "vacations", indexes = {
        @Index(name = "idx_vacation_start_date", columnList = "start_date"),
        @Index(name = "idx_vacation_end_date", columnList = "end_date"),
        @Index(name = "idx_vacation_school_id", columnList = "school_id"),
        @Index(name = "idx_vacation_type", columnList = "vacation_type")
})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vacation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vacation_id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @NotBlank(message = "Vacation name is required")
    @Column(name = "vacation_name", nullable = false, length = 100)
    private String vacationName;

    @NotNull
    @Column(name = "vacation_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private VacationType vacationType;  // HOLIDAY, EXAM_BREAK, SUMMER, WINTER, SPRING, AUTUMN, SPECIAL

    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "notice_sent")
    private Boolean noticeSent;

    @Column(name = "notice_sent_date")
    private LocalDateTime noticeSentDate;

    @Column(name = "created_by")
    private String createdBy;  // Admin username

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum VacationType {
        HOLIDAY("Holiday/Festival"),
        EXAM_BREAK("Exam Break"),
        SUMMER("Summer Vacation"),
        WINTER("Winter Vacation"),
        SPRING("Spring Vacation"),
        AUTUMN("Autumn Vacation"),
        MONSOON("Monsoon Vacation"),
        SPECIAL("Special Leave"),
        EMERGENCY("Emergency Closure");

        private final String label;

        VacationType(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
        if (noticeSent == null) {
            noticeSent = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== HELPER METHODS =====

    public Boolean isOngoing() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    public Boolean isUpcoming() {
        return LocalDate.now().isBefore(startDate);
    }

    public Boolean isCompleted() {
        return LocalDate.now().isAfter(endDate);
    }

    public Long getDurationDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
}

