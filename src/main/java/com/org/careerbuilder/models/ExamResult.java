package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        name = "exam_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_exam_subject",
                        columnNames = {"student_id", "exam_id", "subject_id"}
                )
        },
        indexes = {
                @Index(name = "idx_result_student_exam", columnList = "student_id, exam_id"),
                @Index(name = "idx_result_subject", columnList = "subject_id")
        }
)
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ExamResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id", nullable = false, updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @NotNull @Min(0)
    @Column(name = "obtained_marks", nullable = false)
    private Integer obtainedMarks;

    @NotNull @Min(0)
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Min(0) @Max(100)
    @Column(name = "delta_percent")
    private Integer deltaPercent;

    @Column(name = "grade", length = 5)
    private String grade;

    @Column(name = "rank")
    private Integer rank;

    @Column(name = "feedback", length = 500)
    private String feedback;
}