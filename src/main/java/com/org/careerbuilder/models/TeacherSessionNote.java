package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "teacher_session_notes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_teacher_session_note",
                columnNames = {"faculty_id", "session_kind", "ref_id", "session_date"}
        ),
        indexes = {
                @Index(name = "idx_tsn_faculty_date", columnList = "faculty_id,session_date"),
                @Index(name = "idx_tsn_class_subject", columnList = "class_name,section,subject_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSessionNote {

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

    @NotNull
    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @NotBlank
    @Column(name = "session_kind", nullable = false, length = 30)
    private String sessionKind;

    @NotNull
    @Column(name = "ref_id", nullable = false)
    private Long refId;

    @NotBlank
    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @NotBlank
    @Column(name = "section", nullable = false, length = 10)
    private String section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @NotNull
    @Column(name = "session_start_time", nullable = false)
    private LocalTime sessionStartTime;

    @NotNull
    @Column(name = "session_end_time", nullable = false)
    private LocalTime sessionEndTime;

    @Column(name = "topic_covered", length = 500)
    private String topicCovered;

    @Column(name = "description_notes", columnDefinition = "TEXT")
    private String descriptionNotes;

    @Column(name = "homework", length = 500)
    private String homework;

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
