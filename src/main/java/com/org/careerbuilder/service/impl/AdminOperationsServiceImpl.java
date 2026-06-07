package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.AdminQuickActionRequests;
import com.org.careerbuilder.dto.request.NoticeRequest;
import com.org.careerbuilder.dto.request.VacationRequest;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.AdmissionEnquiry;
import com.org.careerbuilder.models.School;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.models.enums.AdmissionEnquiryStatus;
import com.org.careerbuilder.models.enums.NoticeCategory;
import com.org.careerbuilder.repository.*;
import com.org.careerbuilder.service.AdminOperationsService;
import com.org.careerbuilder.service.AdminTeacherManagementService;
import com.org.careerbuilder.service.NoticeService;
import com.org.careerbuilder.service.VacationService;
import com.org.careerbuilder.service.support.AdminActivityLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminOperationsServiceImpl implements AdminOperationsService {

    private final AdminTeacherManagementService teacherManagementService;
    private final SchoolRepository schoolRepository;
    private final AdmissionEnquiryRepository admissionEnquiryRepository;
    private final NoticeService noticeService;
    private final VacationService vacationService;
    private final AdminActivityLogger activityLogger;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AdminOperationResponses.TeacherCreatedResponse addTeacher(AdminQuickActionRequests.AddTeacherRequest request) {
        return teacherManagementService.quickCreate(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createNotice(AdminQuickActionRequests.CreateNoticeRequest request) {
        NoticeCategory category = mapAudienceToCategory(request.getAudience());
        NoticeRequest noticeRequest = NoticeRequest.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .body(request.getDescription())
                .category(category.name())
                .source(request.getAudience())
                .isPinned(false)
                .attachmentUrls(request.getAttachmentUrl() != null ? java.util.List.of(request.getAttachmentUrl()) : null)
                .build();
        noticeService.createNotice(request.getSchoolId(), noticeRequest);
        activityLogger.log(request.getSchoolId(), AdminActivityType.NOTICE_PUBLISHED,
                "Notice published: " + request.getTitle(),
                request.getAudience(),
                "NOTICE", null, request.getPerformedBy());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addHoliday(AdminQuickActionRequests.AddHolidayRequest request) {
        VacationRequest vacationRequest = VacationRequest.builder()
                .schoolId(request.getSchoolId())
                .vacationName(request.getHolidayName())
                .vacationType("HOLIDAY")
                .startDate(request.getDate())
                .endDate(request.getDate())
                .description(request.getDescription())
                .isActive(true)
                .createdBy(request.getPerformedBy())
                .build();
        var created = vacationService.createVacation(vacationRequest);
        activityLogger.log(request.getSchoolId(), AdminActivityType.HOLIDAY_ADDED,
                "Holiday added: " + request.getHolidayName(),
                request.getDate().toString(),
                "VACATION", created.getVacationId(), request.getPerformedBy());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveAdmissionEnquiry(AdminQuickActionRequests.AdmissionEnquiryRequest request) {
        School school = schoolRepository.findById(request.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found"));
        AdmissionEnquiry enquiry = AdmissionEnquiry.builder()
                .school(school)
                .studentName(request.getStudentName())
                .classApplying(request.getClassApplying())
                .parentName(request.getParentName())
                .phone(request.getPhone())
                .notes(request.getNotes())
                .status(AdmissionEnquiryStatus.PENDING)
                .build();
        enquiry = admissionEnquiryRepository.save(enquiry);
        activityLogger.log(school.getId(), AdminActivityType.ADMISSION_ENQUIRY_SAVED,
                "Admission enquiry: " + request.getStudentName(),
                "Class " + request.getClassApplying(),
                "ADMISSION_ENQUIRY", enquiry.getId(), request.getPerformedBy());
        return enquiry.getId();
    }

    private NoticeCategory mapAudienceToCategory(String audience) {
        if (audience == null) {
            return NoticeCategory.GENERAL;
        }
        String a = audience.toLowerCase(Locale.ROOT);
        if (a.contains("exam")) {
            return NoticeCategory.EXAMS;
        }
        if (a.contains("event")) {
            return NoticeCategory.EVENTS;
        }
        if (a.contains("holiday")) {
            return NoticeCategory.HOLIDAYS;
        }
        if (a.contains("academic") || a.contains("teacher") || a.contains("student")) {
            return NoticeCategory.ACADEMIC;
        }
        return NoticeCategory.GENERAL;
    }

}
