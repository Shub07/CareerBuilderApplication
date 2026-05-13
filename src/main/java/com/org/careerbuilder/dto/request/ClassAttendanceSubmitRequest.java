package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClassAttendanceSubmitRequest {

    @NotBlank
    private String className;

    @NotBlank
    private String section;

    @NotNull
    private Long subjectId;

    @NotNull
    private LocalDate sessionDate;

    @NotEmpty
    private List<ClassAttendanceLineWrite> rows;
}
