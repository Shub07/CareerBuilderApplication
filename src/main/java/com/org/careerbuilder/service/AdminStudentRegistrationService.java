package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminStudentRegistrationRequest;
import com.org.careerbuilder.dto.response.AdminOperationResponses;
import org.springframework.web.multipart.MultipartFile;

public interface AdminStudentRegistrationService {

    AdminOperationResponses.RegistrationMetadataResponse getMetadata(Long schoolId);

    AdminOperationResponses.StudentRegistrationResponse register(AdminStudentRegistrationRequest request);

    String uploadPhoto(Long schoolId, MultipartFile file);

    String uploadDocument(Long schoolId, String documentType, MultipartFile file);

    byte[] generateBulkTemplate();

    AdminOperationResponses.BulkPreviewResponse previewBulkUpload(Long schoolId, MultipartFile file);

    AdminOperationResponses.BulkCommitResponse commitBulkUpload(String previewId, String performedBy);
}
