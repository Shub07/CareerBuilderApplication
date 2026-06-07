package com.org.careerbuilder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherExamFiltersResponse {

    private List<ClassSectionOption> classes;
    private List<SubjectOption> subjects;
    private List<String> examTypes;
    private List<String> statuses;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassSectionOption {
        private String className;
        private String section;
        /** Display label, e.g. "Grade 10 — A" */
        private String label;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubjectOption {
        private Long id;
        private String name;
    }
}
