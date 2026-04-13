package com.org.careerbuilder.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class CareerQuestionsResponse {

    private Long assessmentId;
    private String title;
    private String description;
    private int totalQuestions;
    private int currentQuestionIndex;
    private List<QuestionItem> questions;
    private Map<Integer, Integer> savedAnswers; // questionOrder -> selectedOptionIndex

    @Data
    @Builder
    public static class QuestionItem {
        private Long id;
        private String questionText;
        private int questionOrder;
        private List<OptionItem> options;
    }

    @Data
    @Builder
    public static class OptionItem {
        private Long id;
        private String optionText;
        private int optionIndex;
        private String traitTag;
    }
}
