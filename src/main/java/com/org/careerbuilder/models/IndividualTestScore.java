package com.org.careerbuilder.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "individual_test_scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndividualTestScore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performance_report_id", nullable = false)
    private StudentPerformanceReport performanceReport;
    
    @Column(name = "test_type", nullable = false, length = 50)
    private String testType;  // INTERNAL, WEEKLY_TEST, MID_TERM, FINAL
    
    @Column(name = "marks_obtained", nullable = false)
    private Double marksObtained;
    
    @Column(name = "total_marks", nullable = false)
    private Double totalMarks;
    
    @Column(name = "percentage", nullable = false)
    private Double percentage;
    
    @Column(name = "test_date", nullable = false)
    private LocalDate testDate;
}

