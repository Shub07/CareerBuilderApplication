package com.org.careerbuilder.models;



import com.org.careerbuilder.models.enums.ExamStatus;

import com.org.careerbuilder.models.enums.ExamType;

import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;

import lombok.*;



import java.time.LocalDate;

import java.time.LocalDateTime;

import java.time.LocalTime;



@Entity

@Table(

        name = "exams",

        indexes = {

                @Index(name = "idx_exam_date", columnList = "exam_date"),

                @Index(name = "idx_exam_type", columnList = "exam_type"),

                @Index(name = "idx_exam_school", columnList = "school_id")

        }

)

@Getter @Setter

@NoArgsConstructor

@AllArgsConstructor

@Builder

public class Exam {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "exam_id", nullable = false, updatable = false)

    private Long id;



    @NotBlank

    @Column(name = "exam_name", nullable = false, length = 150)

    private String name;



    @NotNull

    @Column(name = "exam_date", nullable = false)

    private LocalDate examDate;



    @Enumerated(EnumType.STRING)

    @Column(name = "exam_type", nullable = false)

    private ExamType examType;



    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "subject_id")

    private Subject subject;



    @Column(name = "start_time")

    private LocalTime startTime;



    @Column(name = "duration_minutes")

    private Integer durationMinutes;



    @Size(max = 200)

    @Column(name = "venue", length = 200)

    private String venue;



    @Column(name = "school_id")

    private Long schoolId;



    @Column(name = "class_name", length = 50)

    private String className;



    @Column(name = "section", length = 10)

    private String section;



    @Column(name = "end_date")

    private LocalDate endDate;



    @Column(name = "academic_year", length = 20)

    private String academicYear;



    @Column(name = "description", columnDefinition = "TEXT")

    private String description;



    @Column(name = "instructions", columnDefinition = "TEXT")

    private String instructions;



    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "coordinator_id")

    private Faculty coordinator;



    @Enumerated(EnumType.STRING)

    @Column(name = "status", nullable = false, length = 20)

    @Builder.Default

    private ExamStatus status = ExamStatus.DRAFT;



    @Column(name = "deleted", nullable = false)

    @Builder.Default

    private boolean deleted = false;



    @Column(name = "results_published", nullable = false)

    @Builder.Default

    private boolean resultsPublished = false;



    @Column(name = "results_published_at")

    private LocalDateTime resultsPublishedAt;



    @Column(name = "created_at", nullable = false, updatable = false)

    private LocalDateTime createdAt;



    @Column(name = "updated_at")

    private LocalDateTime updatedAt;



    @PrePersist

    protected void onCreate() {

        createdAt = LocalDateTime.now();

        updatedAt = LocalDateTime.now();

        if (status == null) {

            status = ExamStatus.DRAFT;

        }

    }



    @PreUpdate

    protected void onUpdate() {

        updatedAt = LocalDateTime.now();

    }

}

