package com.org.careerbuilder.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 🎫 Vacation Response DTO
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VacationResponse {

    @JsonProperty("vacation_id")
    private Long vacationId;

    @JsonProperty("school_id")
    private Long schoolId;

    @JsonProperty("vacation_name")
    private String vacationName;

    @JsonProperty("vacation_type")
    private String vacationType;

    @JsonProperty("vacation_type_label")
    private String vacationTypeLabel;

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("duration_days")
    private Long durationDays;

    @JsonProperty("description")
    private String description;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("is_ongoing")
    private Boolean isOngoing;

    @JsonProperty("is_upcoming")
    private Boolean isUpcoming;

    @JsonProperty("is_completed")
    private Boolean isCompleted;

    @JsonProperty("notice_sent")
    private Boolean noticeSent;

    @JsonProperty("notice_sent_date")
    private LocalDateTime noticeSentDate;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}

