package com.org.careerbuilder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherExamCardResponse {
    private Long id;
    private String title;
    private String examType;
    private String className;
    private String section;
    private String subjectName;
    private LocalDate examDate;
    private String time;
    /** Same as {@link #venue}; kept for clients that still read "location". */
    private String location;
    /** Room or hall from {@code exams.venue}. */
    private String venue;
    /** MARKS_PENDING, UPCOMING, SUBMITTED */
    private String status;
    private Integer totalMarks;
    private Integer passingMarks;
}
