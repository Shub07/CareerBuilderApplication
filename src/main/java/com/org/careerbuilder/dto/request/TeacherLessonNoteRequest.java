package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherLessonNoteRequest {

    @NotBlank
    private String sessionKind;

    @NotNull
    private Long refId;

    @NotNull
    private LocalDate sessionDate;

    private String topicCovered;
    private String descriptionNotes;
    private String homework;
}
