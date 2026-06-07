package com.org.careerbuilder.dto.request;

import com.org.careerbuilder.models.enums.TeacherActivityType;
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
public class TeacherScheduleEntryRequest {

    @NotBlank
    private String title;

    @NotNull
    private TeacherActivityType activityType;

    /** When true, {@code dayOfWeek} (1=Mon..7=Sun) is required. When false, {@code specificDate} is required. */
    private boolean recurring = true;

    private Integer dayOfWeek;

    private LocalDate specificDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private String className;
    private String section;
    private String sectionLabel;
    private String venue;
    private String notes;
    private Long subjectId;

    /** Optional substitution label shown on schedule cards, e.g. colleague first name. */
    private String substituteForName;
}
