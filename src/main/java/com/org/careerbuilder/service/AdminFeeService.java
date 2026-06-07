package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminFeeRequests;
import com.org.careerbuilder.dto.response.AdminFeeDtos;

import java.util.List;

public interface AdminFeeService {

    // List page
    AdminFeeDtos.FeeStatsResponse getStats(Long schoolId);

    AdminFeeDtos.StudentFeeListResponse listStudentFees(
            Long schoolId, String q, String className, String status, int page, int size);

    AdminFeeDtos.FeeFilterOptionsResponse getFilterOptions(Long schoolId);

    // Fee structures
    AdminFeeDtos.FeeStructureListResponse getStructures(Long schoolId);

    AdminFeeDtos.FeeStructureRow createStructure(AdminFeeRequests.CreateFeeStructureRequest request);

    AdminFeeDtos.FeeStructureRow updateStructure(Long structureId, AdminFeeRequests.UpdateFeeStructureRequest request);

    AdminFeeDtos.ActionResponse deleteStructure(Long schoolId, Long structureId, String performedBy);

    // Collect / refund
    List<AdminFeeDtos.StudentSearchOption> searchStudents(Long schoolId, String q);

    List<AdminFeeDtos.PaidFeeOption> getPaidFees(Long schoolId, Long studentId);

    AdminFeeDtos.CollectFeeResponse collectFee(AdminFeeRequests.CollectFeeRequest request, String idempotencyKey);

    AdminFeeDtos.RefundResponse refundFee(AdminFeeRequests.RefundFeeRequest request, String idempotencyKey);

    // Student detail + receipt
    AdminFeeDtos.StudentFeeDetailResponse getStudentDetail(Long schoolId, Long studentId);

    AdminFeeDtos.ReceiptResponse getReceipt(Long schoolId, Long transactionId);

    byte[] getReceiptPdf(Long schoolId, Long transactionId);

    AdminFeeDtos.ActionResponse remind(Long schoolId, Long studentId, String performedBy);

    // Export
    byte[] exportReport(AdminFeeRequests.ExportFeeReportRequest request);

    String exportContentType(String format);

    String exportFilename(String format);
}
