package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherExtraSessionRequest {

    @NotBlank
    private String className;

    @NotBlank
    private String section;

    @NotNull
    private Long subjectId;

    @NotNull
    private LocalDate sessionDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private String reason;

    /** Shown as "Handling for {name}" when set. */
    private String substituteForName;
}
