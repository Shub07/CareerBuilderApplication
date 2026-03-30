package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "student_performance_reports", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "subject_id", "exam_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentPerformanceReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;
    
    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;
    
    @Column(name = "total_marks", nullable = false)
    private Double totalMarks;
    
    @Column(name = "percentage", nullable = false)
    private Double percentage;
    
    @Column(name = "grade", nullable = false, length = 5)
    private String grade;
    
    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;
    
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;
    
    @Column(name = "performance_status", nullable = false, length = 20)
    private String performanceStatus;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "academic_year", nullable = false)
    private Integer academicYear;
    
    @OneToMany(mappedBy = "performanceReport", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<IndividualTestScore> individualTestScores;
    
    @Transient
    @JsonProperty("studentId")
    private Long studentId;
    
    @Transient
    @JsonProperty("subjectId")
    private Long subjectId;
    
    @Transient
    @JsonProperty("examId")
    private Long examId;
}
