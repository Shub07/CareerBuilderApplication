package com.org.careerbuilder.dto.dashboard;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ExamSubjectScore {

    private String subject;
    private String marks;
    private String grade;
    private String delta;
    private LocalDate date;
}
