package com.org.careerbuilder.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMarksHistoryResponse {
    private Long examId;
    private String examTitle;
    private String className;
    private String section;
    private String subjectName;
    private LocalDate examDate;
    private LocalDateTime submittedOn;
    private Integer totalStudents;
    private Integer presentCount;
    private Double averageMarks;
    private Integer passCount;
    private Integer failCount;
    private String marksStatus; // SUBMITTED, LOCKED
}

