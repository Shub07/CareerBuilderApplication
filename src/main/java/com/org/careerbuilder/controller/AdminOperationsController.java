package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.AdminQuickActionRequests;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.service.AdminOperationsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminOperationsController {

    private final AdminOperationsService adminOperationsService;

    /** Add Teacher modal */
    @PostMapping("/teachers")
    public ResponseEntity<AdminOperationResponses.TeacherCreatedResponse> addTeacher(
            @Valid @RequestBody AdminQuickActionRequests.AddTeacherRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminOperationsService.addTeacher(request));
    }

    /** Create Notice modal */
    @PostMapping("/notices")
    public ResponseEntity<Map<String, Object>> createNotice(
            @Valid @RequestBody AdminQuickActionRequests.CreateNoticeRequest request) {
        adminOperationsService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Notice published successfully"
        ));
    }

    /** Add Holiday modal */
    @PostMapping("/holidays")
    public ResponseEntity<Map<String, Object>> addHoliday(
            @Valid @RequestBody AdminQuickActionRequests.AddHolidayRequest request) {
        adminOperationsService.addHoliday(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Holiday added successfully"
        ));
    }

    /** New Admission / Save Enquiry modal */
    @PostMapping("/admissions/enquiries")
    public ResponseEntity<Map<String, Object>> saveEnquiry(
            @Valid @RequestBody AdminQuickActionRequests.AdmissionEnquiryRequest request) {
        Long id = adminOperationsService.saveAdmissionEnquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Enquiry saved successfully",
                "enquiryId", id
        ));
    }
}
