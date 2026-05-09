package com.org.careerbuilder.dto.response;

import lombok.*;

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
    private String location;
    private String status; // MARKS_PENDING, UPCOMING, SUBMITTED
    private Integer totalMarks;
    private Integer passingMarks;
}

