package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.TeacherActivityType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "teacher_schedule_entries", indexes = {
        @Index(name = "idx_tse_faculty", columnList = "faculty_id"),
        @Index(name = "idx_tse_school", columnList = "school_id"),
        @Index(name = "idx_tse_specific_date", columnList = "specific_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherScheduleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @NotNull
    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @NotBlank
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private TeacherActivityType activityType;

    @Column(name = "recurring", nullable = false)
    private boolean recurring = true;

    @Min(1)
    @Max(7)
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "specific_date")
    private LocalDate specificDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "section", length = 10)
    private String section;

    @Column(name = "section_label", length = 120)
    private String sectionLabel;

    @Column(name = "venue", length = 200)
    private String venue;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Optional substitution label, e.g. "John" for "Handling for John". */
    @Column(name = "substitute_for_name", length = 120)
    private String substituteForName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
