package com.org.careerbuilder.models;

import com.org.careerbuilder.models.enums.AssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_submissions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assign_student",
                        columnNames = {"assignment_id", "student_id"})
        })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private String filePath;

    private String comments;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    private LocalDateTime submittedAt;

    @Column(name = "letter_grade", length = 5)
    private String letterGrade;

    @Column(name = "points_obtained")
    private Integer pointsObtained;

    @Column(name = "points_total")
    private Integer pointsTotal;

    @Column(name = "teacher_remarks", columnDefinition = "TEXT")
    private String teacherRemarks;

    // Explicit getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }
    
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getLetterGrade() { return letterGrade; }
    public void setLetterGrade(String letterGrade) { this.letterGrade = letterGrade; }

    public Integer getPointsObtained() { return pointsObtained; }
    public void setPointsObtained(Integer pointsObtained) { this.pointsObtained = pointsObtained; }

    public Integer getPointsTotal() { return pointsTotal; }
    public void setPointsTotal(Integer pointsTotal) { this.pointsTotal = pointsTotal; }

    public String getTeacherRemarks() { return teacherRemarks; }
    public void setTeacherRemarks(String teacherRemarks) { this.teacherRemarks = teacherRemarks; }
}