package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminQuickActionRequests;
import com.org.careerbuilder.dto.response.AdminOperationResponses;

public interface AdminOperationsService {

    AdminOperationResponses.TeacherCreatedResponse addTeacher(AdminQuickActionRequests.AddTeacherRequest request);

    void createNotice(AdminQuickActionRequests.CreateNoticeRequest request);

    void addHoliday(AdminQuickActionRequests.AddHolidayRequest request);

    Long saveAdmissionEnquiry(AdminQuickActionRequests.AdmissionEnquiryRequest request);
}
