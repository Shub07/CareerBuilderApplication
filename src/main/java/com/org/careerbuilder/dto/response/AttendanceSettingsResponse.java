package com.org.careerbuilder.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO for attendance settings response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSettingsResponse {

    private Long settingId;

    private Long schoolId;

    private Integer workingDaysPerWeek;

    private Integer minAttendancePercentage;

    private Boolean allowBulkMarking;

    private Boolean allowRetroactiveMarking;

    private Integer maxRetroactiveDays;

    private Boolean notifyLowAttendance;

    private Integer lowAttendanceThreshold;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

