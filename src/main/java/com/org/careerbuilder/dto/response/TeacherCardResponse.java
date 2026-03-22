package com.org.careerbuilder.dto.response;

public record TeacherCardResponse(
        Long teacherId,
        String teacherName,
        String subjectName,
        int experienceYears,
        String qualification,
        String availabilityTag,
        String classesText
) {}