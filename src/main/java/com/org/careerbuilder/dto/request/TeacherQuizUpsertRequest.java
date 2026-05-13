package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.QuizLifecycleStatus;
import com.org.careerbuilder.models.enums.QuizQuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class TeacherQuizUpsertRequest {

    @NotBlank
    @Size(max = 300)
    private String title;

    private String instructions;

    @NotBlank
    @Size(max = 50)
    private String className;

    @NotBlank
    @Size(max = 10)
    private String section;

    @NotNull
    private Long subjectId;

    @NotNull
    @Min(1)
    @Max(600)
    private Integer timeLimitMinutes;

    /**
     * Optional; if absent, derived as the sum of question max marks.
     */
    @Min(1)
    @Max(10000)
    private Integer totalMarks;

    private LocalDateTime scheduledAt;

    private Boolean shuffleQuestions;

    private QuizLifecycleStatus lifecycleStatus;

    @NotEmpty
    @Valid
    private List<TeacherQuizQuestionRequest> questions = new ArrayList<>();

    @Data
    public static class TeacherQuizQuestionRequest {

        @NotNull
        private Integer sortOrder;

        @NotNull
        private QuizQuestionType questionType;

        @NotBlank
        private String questionText;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal maxMarks;

        private String answerKey;

        @Valid
        private List<TeacherQuizOptionRequest> options = new ArrayList<>();
    }

    @Data
    public static class TeacherQuizOptionRequest {

        @NotNull
        private Integer sortOrder;

        @NotBlank
        @Size(max = 500)
        private String optionText;

        @NotNull
        private Boolean correct;
    }
}
