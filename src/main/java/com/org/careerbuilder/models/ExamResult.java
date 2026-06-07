package com.org.careerbuilder.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

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
@NoArgsConstructor
@AllArgsConstructor
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

    /** Null means marks not entered yet (draft / pending row). */
    @Min(0)
    @Column(name = "obtained_marks", nullable = true)
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

    // Marks management fields
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "marks_locked")
    private Boolean marksLocked = false;

    public boolean isMarksLocked() {
        return Boolean.TRUE.equals(marksLocked);
    }

    public void setMarksLocked(boolean marksLocked) {
        this.marksLocked = marksLocked;
    }

    @Column(name = "marks_locked_at")
    private LocalDateTime marksLockedAt;

    @Column(name = "marks_edited_at")
    private LocalDateTime marksEditedAt;

    @Column(name = "marks_edit_reason", columnDefinition = "TEXT")
    private String marksEditReason;

    @Column(name = "edited_by_teacher_id")
    private Long editedByTeacherId;

    @Column(name = "admin_verified", nullable = false)
    @Builder.Default
    private boolean adminVerified = false;

    // Explicit getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    
    public Exam getExam() { return exam; }
    public void setExam(Exam exam) { this.exam = exam; }
    
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    
    public Integer getObtainedMarks() { return obtainedMarks; }
    public void setObtainedMarks(Integer obtainedMarks) { this.obtainedMarks = obtainedMarks; }
    
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    
    public Integer getDeltaPercent() { return deltaPercent; }
    public void setDeltaPercent(Integer deltaPercent) { this.deltaPercent = deltaPercent; }
}