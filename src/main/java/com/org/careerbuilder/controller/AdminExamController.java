package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.AdminExamRequests;
import com.org.careerbuilder.dto.response.AdminExamDtos;
import com.org.careerbuilder.security.AdminSecurityContext;
import com.org.careerbuilder.service.AdminExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/exams")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SCHOOL_ADMIN')")
public class AdminExamController {

    private final AdminExamService examService;

    @GetMapping("/stats")
    public ResponseEntity<AdminExamDtos.ExamStatsResponse> stats() {
        return ResponseEntity.ok(examService.getStats(AdminSecurityContext.requireSchoolId()));
    }

    @GetMapping
    public ResponseEntity<AdminExamDtos.ExamListResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(name = "class", required = false) String className,
            @RequestParam(required = false) String examType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String academicYear,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long schoolId = AdminSecurityContext.requireSchoolId();
        return ResponseEntity.ok(examService.listExams(
                schoolId, q, className, examType, status, academicYear, page, size));
    }

    @GetMapping("/filters")
    public ResponseEntity<AdminExamDtos.ExamFilterOptionsResponse> filters() {
        return ResponseEntity.ok(examService.getFilterOptions(AdminSecurityContext.requireSchoolId()));
    }

    @PostMapping
    public ResponseEntity<AdminExamDtos.ExamDetailResponse> create(
            @Valid @RequestBody AdminExamRequests.CreateExamRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.createExam(request));
    }

    @GetMapping("/{examId}")
    public ResponseEntity<AdminExamDtos.ExamDetailResponse> detail(@PathVariable Long examId) {
        return ResponseEntity.ok(examService.getExamDetail(AdminSecurityContext.requireSchoolId(), examId));
    }

    @PutMapping("/{examId}")
    public ResponseEntity<AdminExamDtos.ExamDetailResponse> update(
            @PathVariable Long examId,
            @Valid @RequestBody AdminExamRequests.UpdateExamRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.updateExam(examId, request));
    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<AdminExamDtos.ActionResponse> delete(@PathVariable Long examId) {
        Long schoolId = AdminSecurityContext.requireSchoolId();
        return ResponseEntity.ok(examService.deleteExam(
                schoolId, examId, AdminSecurityContext.performerName()));
    }

    @GetMapping("/{examId}/timetable")
    public ResponseEntity<AdminExamDtos.TimetableResponse> timetable(@PathVariable Long examId) {
        return ResponseEntity.ok(examService.getTimetable(AdminSecurityContext.requireSchoolId(), examId));
    }

    @PostMapping("/{examId}/timetable")
    public ResponseEntity<AdminExamDtos.ScheduleRow> addSchedule(
            @PathVariable Long examId,
            @Valid @RequestBody AdminExamRequests.CreateScheduleRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.addSchedule(examId, request));
    }

    @PutMapping("/timetable/{scheduleId}")
    public ResponseEntity<AdminExamDtos.ScheduleRow> updateSchedule(
            @PathVariable Long scheduleId,
            @Valid @RequestBody AdminExamRequests.UpdateScheduleRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.updateSchedule(scheduleId, request));
    }

    @DeleteMapping("/timetable/{scheduleId}")
    public ResponseEntity<AdminExamDtos.ActionResponse> deleteSchedule(@PathVariable Long scheduleId) {
        Long schoolId = AdminSecurityContext.requireSchoolId();
        return ResponseEntity.ok(examService.deleteSchedule(
                schoolId, scheduleId, AdminSecurityContext.performerName()));
    }

    @GetMapping("/venues")
    public ResponseEntity<List<AdminExamDtos.VenueRow>> venues() {
        return ResponseEntity.ok(examService.listVenues(AdminSecurityContext.requireSchoolId()));
    }

    @GetMapping("/venues/{venueId}/availability")
    public ResponseEntity<AdminExamDtos.VenueAvailabilityResponse> venueAvailability(
            @PathVariable Long venueId,
            @RequestParam LocalDate date,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime,
            @RequestParam(required = false) Long excludeScheduleId) {
        return ResponseEntity.ok(examService.venueAvailability(
                AdminSecurityContext.requireSchoolId(), venueId, date, startTime, endTime, excludeScheduleId));
    }

    @GetMapping("/{examId}/sections")
    public ResponseEntity<List<AdminExamDtos.SectionOption>> sections(@PathVariable Long examId) {
        return ResponseEntity.ok(examService.examSections(AdminSecurityContext.requireSchoolId(), examId));
    }

    @PostMapping("/{examId}/students/preview")
    public ResponseEntity<AdminExamDtos.AssignmentPreviewResponse> previewAssignment(
            @PathVariable Long examId,
            @Valid @RequestBody AdminExamRequests.AssignmentPreviewRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(examService.previewAssignment(
                AdminSecurityContext.requireSchoolId(), examId, request, page, size));
    }

    @PostMapping("/{examId}/students/assign")
    public ResponseEntity<AdminExamDtos.BulkAssignResponse> assignStudents(
            @PathVariable Long examId,
            @Valid @RequestBody AdminExamRequests.BulkAssignStudentsRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.assignStudents(examId, request));
    }

    @GetMapping("/{examId}/students")
    public ResponseEntity<AdminExamDtos.AssignedStudentsResponse> assignedStudents(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(examService.listAssignedStudents(
                AdminSecurityContext.requireSchoolId(), examId, page, size));
    }

    @PostMapping("/{examId}/publish-results")
    public ResponseEntity<AdminExamDtos.PublishResultsResponse> publishResults(
            @PathVariable Long examId,
            @Valid @RequestBody AdminExamRequests.PublishResultsRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.publishResults(examId, request));
    }

    @PostMapping("/{examId}/reports/export")
    public ResponseEntity<byte[]> exportReport(
            @PathVariable Long examId,
            @RequestBody AdminExamRequests.ExportExamReportRequest request) {
        AdminExamDtos.ExportReportResponse export = examService.exportReport(
                AdminSecurityContext.requireSchoolId(), examId, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + export.fileName() + "\"")
                .contentType(MediaType.parseMediaType(export.contentType()))
                .body(export.content());
    }

    @GetMapping("/{examId}/activity-log")
    public ResponseEntity<AdminExamDtos.ActivityLogResponse> activityLog(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(examService.examActivityLog(
                AdminSecurityContext.requireSchoolId(), examId, page, size));
    }

    // ─── Hall tickets ───────────────────────────────────────────────────────────

    @GetMapping("/{examId}/hall-tickets")
    public ResponseEntity<AdminExamDtos.HallTicketListResponse> hallTickets(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(examService.listHallTickets(
                AdminSecurityContext.requireSchoolId(), examId, page, size));
    }

    @PostMapping("/{examId}/hall-tickets/preview")
    public ResponseEntity<AdminExamDtos.HallTicketGeneratePreviewResponse> previewHallTickets(
            @PathVariable Long examId,
            @Valid @RequestBody AdminExamRequests.HallTicketGeneratePreviewRequest request) {
        return ResponseEntity.ok(examService.previewHallTicketGeneration(
                AdminSecurityContext.requireSchoolId(), examId, request));
    }

    @PostMapping("/{examId}/hall-tickets/generate")
    public ResponseEntity<AdminExamDtos.HallTicketGenerateResponse> generateHallTickets(
            @PathVariable Long examId,
            @Valid @RequestBody AdminExamRequests.GenerateHallTicketsRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.generateHallTickets(examId, request));
    }

    @GetMapping("/{examId}/hall-tickets/batch-pdf")
    public ResponseEntity<byte[]> hallTicketsBatchPdf(@PathVariable Long examId) {
        Long schoolId = AdminSecurityContext.requireSchoolId();
        byte[] pdf = examService.downloadHallTicketBatchPdf(schoolId, examId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"hall-tickets-batch.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/{examId}/hall-tickets/{registrationId}")
    public ResponseEntity<AdminExamDtos.HallTicketPreviewResponse> hallTicketPreview(
            @PathVariable Long examId,
            @PathVariable Long registrationId) {
        return ResponseEntity.ok(examService.getHallTicketPreview(
                AdminSecurityContext.requireSchoolId(), examId, registrationId));
    }

    @GetMapping("/{examId}/hall-tickets/{registrationId}/pdf")
    public ResponseEntity<byte[]> hallTicketPdf(
            @PathVariable Long examId,
            @PathVariable Long registrationId) {
        Long schoolId = AdminSecurityContext.requireSchoolId();
        byte[] pdf = examService.downloadHallTicketPdf(schoolId, examId, registrationId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"hall-ticket-" + registrationId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ─── Venue allocation ───────────────────────────────────────────────────────

    @GetMapping("/{examId}/venue-allocations")
    public ResponseEntity<AdminExamDtos.VenueAllocationSummaryResponse> venueAllocations(
            @PathVariable Long examId) {
        return ResponseEntity.ok(examService.getVenueAllocations(
                AdminSecurityContext.requireSchoolId(), examId));
    }

    @PostMapping("/{examId}/venue-allocations")
    public ResponseEntity<AdminExamDtos.VenueAllocationDetailResponse> assignVenue(
            @PathVariable Long examId,
            @Valid @RequestBody AdminExamRequests.AssignVenueAllocationRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examService.assignVenueAllocation(examId, request));
    }

    @PutMapping("/venue-allocations/{allocationId}")
    public ResponseEntity<AdminExamDtos.VenueAllocationDetailResponse> updateVenueAllocation(
            @PathVariable Long allocationId,
            @Valid @RequestBody AdminExamRequests.UpdateVenueAllocationRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.updateVenueAllocation(allocationId, request));
    }

    @DeleteMapping("/venue-allocations/{allocationId}")
    public ResponseEntity<AdminExamDtos.ActionResponse> deleteVenueAllocation(
            @PathVariable Long allocationId) {
        Long schoolId = AdminSecurityContext.requireSchoolId();
        return ResponseEntity.ok(examService.deleteVenueAllocation(
                schoolId, allocationId, AdminSecurityContext.performerName()));
    }

    // ─── Marks monitoring ───────────────────────────────────────────────────────

    @GetMapping("/{examId}/marks-monitoring")
    public ResponseEntity<AdminExamDtos.MarksMonitoringResponse> marksMonitoring(@PathVariable Long examId) {
        return ResponseEntity.ok(examService.marksMonitoring(AdminSecurityContext.requireSchoolId(), examId));
    }

    @GetMapping("/{examId}/marks/{subjectId}")
    public ResponseEntity<AdminExamDtos.SubjectMarksDetailResponse> subjectMarks(
            @PathVariable Long examId,
            @PathVariable Long subjectId) {
        return ResponseEntity.ok(examService.subjectMarksDetail(
                AdminSecurityContext.requireSchoolId(), examId, subjectId));
    }

    @PostMapping("/{examId}/marks/{subjectId}/approve")
    public ResponseEntity<AdminExamDtos.ApproveMarksResponse> approveMarks(
            @PathVariable Long examId,
            @PathVariable Long subjectId,
            @Valid @RequestBody AdminExamRequests.ApproveSubjectMarksRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.approveSubjectMarks(examId, subjectId, request));
    }

    @PostMapping("/{examId}/marks/{subjectId}/reject")
    public ResponseEntity<AdminExamDtos.ApproveMarksResponse> rejectMarks(
            @PathVariable Long examId,
            @PathVariable Long subjectId,
            @Valid @RequestBody AdminExamRequests.RejectSubjectMarksRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.rejectSubjectMarks(examId, subjectId, request));
    }

    @PutMapping("/{examId}/marks/{subjectId}/draft")
    public ResponseEntity<AdminExamDtos.ActionResponse> saveMarksDraft(
            @PathVariable Long examId,
            @PathVariable Long subjectId,
            @RequestBody AdminExamRequests.SaveMarksDraftRequest request) {
        AdminSecurityContext.stamp(request);
        return ResponseEntity.ok(examService.saveSubjectMarksDraft(examId, subjectId, request));
    }

    // ─── Final results ──────────────────────────────────────────────────────────

    @GetMapping("/{examId}/results/summary")
    public ResponseEntity<AdminExamDtos.ExamResultsSummaryResponse> resultsSummary(@PathVariable Long examId) {
        return ResponseEntity.ok(examService.resultsSummary(AdminSecurityContext.requireSchoolId(), examId));
    }

    @GetMapping("/{examId}/results")
    public ResponseEntity<AdminExamDtos.ExamResultsListResponse> finalResults(
            @PathVariable Long examId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(examService.listFinalResults(
                AdminSecurityContext.requireSchoolId(), examId, page, size));
    }

    @PostMapping("/{examId}/results/print")
    public ResponseEntity<byte[]> printResults(
            @PathVariable Long examId,
            @RequestBody AdminExamRequests.PrintResultsRequest request) {
        byte[] pdf = examService.printResults(AdminSecurityContext.requireSchoolId(), examId, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"exam-results.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
