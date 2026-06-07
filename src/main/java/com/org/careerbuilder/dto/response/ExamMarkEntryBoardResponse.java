package com.org.careerbuilder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    /** Room or hall from {@code exams.venue}. */
    private String venue;
    private Integer totalMarks;
    private Integer passingMarks;
    private Integer totalStudents;
    private Integer presentCount;
    private List<ExamMarkEntryRowResponse> students;
    private Boolean isLocked;
    /** IN_PROGRESS or SUBMITTED (mirrors UI banner state) */
    private String status;
}
