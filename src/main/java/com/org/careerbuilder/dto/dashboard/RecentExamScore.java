package com.org.careerbuilder.dto.dashboard;

import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecentExamScore {

    private String totalMarksText;
    private List<ExamSubjectScore> subjects;
}
