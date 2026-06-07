package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminExamRequests;
import com.org.careerbuilder.dto.response.AdminExamDtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AdminExamService {

    AdminExamDtos.ExamStatsResponse getStats(Long schoolId);

    AdminExamDtos.ExamListResponse listExams(
            Long schoolId, String q, String className, String examType,
            String status, String academicYear, int page, int size);

    AdminExamDtos.ExamFilterOptionsResponse getFilterOptions(Long schoolId);

    AdminExamDtos.ExamDetailResponse createExam(AdminExamRequests.CreateExamRequest request);

    AdminExamDtos.ExamDetailResponse updateExam(Long examId, AdminExamRequests.UpdateExamRequest request);

    AdminExamDtos.ActionResponse deleteExam(Long schoolId, Long examId, String performedBy);

    AdminExamDtos.ExamDetailResponse getExamDetail(Long schoolId, Long examId);

    AdminExamDtos.TimetableResponse getTimetable(Long schoolId, Long examId);

    AdminExamDtos.ScheduleRow addSchedule(Long examId, AdminExamRequests.CreateScheduleRequest request);

    AdminExamDtos.ScheduleRow updateSchedule(
            Long scheduleId, AdminExamRequests.UpdateScheduleRequest request);

    AdminExamDtos.ActionResponse deleteSchedule(Long schoolId, Long scheduleId, String performedBy);

    List<AdminExamDtos.VenueRow> listVenues(Long schoolId);

    AdminExamDtos.VenueAvailabilityResponse venueAvailability(
            Long schoolId, Long venueId, LocalDate date, LocalTime startTime, LocalTime endTime, Long excludeScheduleId);

    List<AdminExamDtos.SectionOption> examSections(Long schoolId, Long examId);

    AdminExamDtos.AssignmentPreviewResponse previewAssignment(
            Long schoolId, Long examId, AdminExamRequests.AssignmentPreviewRequest request, int page, int size);

    AdminExamDtos.BulkAssignResponse assignStudents(Long examId, AdminExamRequests.BulkAssignStudentsRequest request);

    AdminExamDtos.AssignedStudentsResponse listAssignedStudents(Long schoolId, Long examId, int page, int size);

    AdminExamDtos.PublishResultsResponse publishResults(Long examId, AdminExamRequests.PublishResultsRequest request);

    AdminExamDtos.ExportReportResponse exportReport(
            Long schoolId, Long examId, AdminExamRequests.ExportExamReportRequest request);

    AdminExamDtos.ActivityLogResponse examActivityLog(Long schoolId, Long examId, int page, int size);

    AdminExamDtos.HallTicketListResponse listHallTickets(Long schoolId, Long examId, int page, int size);

    AdminExamDtos.HallTicketGeneratePreviewResponse previewHallTicketGeneration(
            Long schoolId, Long examId, AdminExamRequests.HallTicketGeneratePreviewRequest request);

    AdminExamDtos.HallTicketGenerateResponse generateHallTickets(
            Long examId, AdminExamRequests.GenerateHallTicketsRequest request);

    AdminExamDtos.HallTicketPreviewResponse getHallTicketPreview(
            Long schoolId, Long examId, Long registrationId);

    byte[] downloadHallTicketPdf(Long schoolId, Long examId, Long registrationId);

    byte[] downloadHallTicketBatchPdf(Long schoolId, Long examId);

    AdminExamDtos.VenueAllocationSummaryResponse getVenueAllocations(Long schoolId, Long examId);

    AdminExamDtos.VenueAllocationDetailResponse assignVenueAllocation(
            Long examId, AdminExamRequests.AssignVenueAllocationRequest request);

    AdminExamDtos.VenueAllocationDetailResponse updateVenueAllocation(
            Long allocationId, AdminExamRequests.UpdateVenueAllocationRequest request);

    AdminExamDtos.ActionResponse deleteVenueAllocation(Long schoolId, Long allocationId, String performedBy);

    AdminExamDtos.MarksMonitoringResponse marksMonitoring(Long schoolId, Long examId);

    AdminExamDtos.SubjectMarksDetailResponse subjectMarksDetail(Long schoolId, Long examId, Long subjectId);

    AdminExamDtos.ApproveMarksResponse approveSubjectMarks(
            Long examId, Long subjectId, AdminExamRequests.ApproveSubjectMarksRequest request);

    AdminExamDtos.ApproveMarksResponse rejectSubjectMarks(
            Long examId, Long subjectId, AdminExamRequests.RejectSubjectMarksRequest request);

    AdminExamDtos.ActionResponse saveSubjectMarksDraft(
            Long examId, Long subjectId, AdminExamRequests.SaveMarksDraftRequest request);

    AdminExamDtos.ExamResultsSummaryResponse resultsSummary(Long schoolId, Long examId);

    AdminExamDtos.ExamResultsListResponse listFinalResults(Long schoolId, Long examId, int page, int size);

    byte[] printResults(Long schoolId, Long examId, AdminExamRequests.PrintResultsRequest request);
}
