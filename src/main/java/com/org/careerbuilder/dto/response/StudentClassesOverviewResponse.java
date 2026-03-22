package com.org.careerbuilder.dto.response;

public record StudentClassesOverviewResponse(
        int totalClasses,
        int completedClasses,
        int totalHoursText,
        String breakHoursText,
        int specialClasses
) {}