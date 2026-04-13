package com.org.careerbuilder.dto.request;

import lombok.*;

/**
 * DTO for updating attendance settings for a school
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSettingsRequest {

    private Integer workingDaysPerWeek;

    private Integer minAttendancePercentage;

    private Boolean allowBulkMarking;

    private Boolean allowRetroactiveMarking;

    private Integer maxRetroactiveDays;

    private Boolean notifyLowAttendance;

    private Integer lowAttendanceThreshold;
}

