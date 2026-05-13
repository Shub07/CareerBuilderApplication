package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.TeacherDailyActivityReviewRequest;
import com.org.careerbuilder.dto.response.TeacherDailyActivityDtos;

import java.time.LocalDate;
import java.util.List;

public interface TeacherDailyActivityService {
    List<TeacherDailyActivityDtos.ClassSectionFilter> getFilters(Long facultyId);

    TeacherDailyActivityDtos.DailyStudentListResponse getStudentList(
            Long facultyId,
            String className,
            String section,
            String range,
            LocalDate date,
            String search
    );

    TeacherDailyActivityDtos.StudentDetailResponse getStudentDetail(
            Long facultyId,
            Long studentId,
            LocalDate date
    );

    TeacherDailyActivityDtos.ReviewResponse upsertReview(
            Long facultyId,
            TeacherDailyActivityReviewRequest request
    );
}
