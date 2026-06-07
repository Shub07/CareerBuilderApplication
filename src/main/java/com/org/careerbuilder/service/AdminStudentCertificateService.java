package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.AdminStudentRequests;
import com.org.careerbuilder.dto.response.AdminStudentCertificateDtos;
import org.springframework.web.multipart.MultipartFile;

public interface AdminStudentCertificateService {

    AdminStudentCertificateDtos.CertificatesListResponse listCertificates(
            Long schoolId, Long studentId, String search, String category);

    AdminStudentCertificateDtos.CertificateDetailResponse getCertificateDetail(
            Long schoolId, Long studentId, Long certificateId);

    byte[] downloadCertificate(Long schoolId, Long studentId, Long certificateId);

    AdminStudentCertificateDtos.CertificateUploadResponse uploadCertificate(
            Long schoolId, Long studentId, MultipartFile file, AdminStudentRequests.UploadCertificateRequest meta);

    void deleteCertificate(Long schoolId, Long studentId, Long certificateId, String performedBy);

    byte[] downloadBulkTemplate();

    AdminStudentCertificateDtos.BulkCertificatePreviewResponse previewBulkUpload(
            Long schoolId, Long studentId, MultipartFile excelFile, MultipartFile filesArchive);

    AdminStudentCertificateDtos.BulkCertificateCommitResponse commitBulkUpload(
            Long schoolId, Long studentId, String previewId, String performedBy);
}
