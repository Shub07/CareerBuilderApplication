package com.org.careerbuilder.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 🎫 Vacation Create/Update Request DTO
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacationRequest {

    @NotNull(message = "School ID is required")
    @JsonProperty("school_id")
    private Long schoolId;

    @NotBlank(message = "Vacation name is required")
    @Size(min = 3, max = 100, message = "Vacation name must be between 3 and 100 characters")
    @JsonProperty("vacation_name")
    private String vacationName;

    @NotNull(message = "Vacation type is required")
    @JsonProperty("vacation_type")
    private String vacationType;  // HOLIDAY, EXAM_BREAK, SUMMER, WINTER, SPRING, AUTUMN, SPECIAL, EMERGENCY

    @NotNull(message = "Start date is required")
    @JsonProperty("start_date")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonProperty("end_date")
    private LocalDate endDate;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @JsonProperty("description")
    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("created_by")
    private String createdBy;
}

