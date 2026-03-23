package com.org.careerbuilder.dto.response;

import java.util.List;

/**
 * DTO for detailed Teacher Profile View (shown when user clicks "View Profile")
 * Contains complete teacher information for the profile page
 */
public record TeacherProfileResponse(
        Long teacherId,
        String teacherName,
        String subjectName,
        String profilePhotoUrl,
        String qualification,
        Integer experienceYears,
        String about,
        String email,
        String phone,
        List<String> classesTaught,
        OfficeHoursDTO officeHours,
        String availabilityStatus,
        boolean messageEnabled
) {
    public record OfficeHoursDTO(
            String day,           // e.g., "Mon - Fri"
            String startTime,     // e.g., "3:30 PM"
            String endTime        // e.g., "4:30 PM"
    ) {}
}

