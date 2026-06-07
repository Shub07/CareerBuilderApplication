package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.AdminStudentCertificateDtos;

public interface AdminStudentActivityLogService {

    AdminStudentCertificateDtos.ActivityLogFeedResponse getActivityLog(
            Long schoolId, Long studentId, String search, String logType, int page, int size);

    AdminStudentCertificateDtos.ActivityLogFilterOptions getFilterOptions();
}
