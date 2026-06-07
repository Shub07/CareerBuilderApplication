package com.org.careerbuilder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    /** Room or hall from {@code exams.venue}. */
    private String venue;
    private LocalDateTime submittedOn;
    private Integer totalStudents;
    private Integer presentCount;
    private Double averageMarks;
    private Integer passCount;
    private Integer failCount;
    private String marksStatus;
}
