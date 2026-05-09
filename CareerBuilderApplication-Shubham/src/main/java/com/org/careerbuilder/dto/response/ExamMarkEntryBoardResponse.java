package com.org.careerbuilder.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMarkEntryBoardResponse {
    private Long examId;
    private String examTitle;
    private String className;
    private String section;
    private String subjectName;
    private LocalDate examDate;
    private Integer totalMarks;
    private Integer passingMarks;
    private Integer totalStudents;
    private Integer presentCount;
    private List<ExamMarkEntryRowResponse> students;
    private Boolean isLocked;
    private String status; // IN_PROGRESS, SUBMITTED
}

