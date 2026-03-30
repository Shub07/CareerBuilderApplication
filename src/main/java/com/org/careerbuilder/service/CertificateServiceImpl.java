package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.CertificateGroupResponse;
import com.org.careerbuilder.dto.response.CertificateResponse;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.Certificate;
import com.org.careerbuilder.models.Certificate.CertificateCategory;
import com.org.careerbuilder.repository.CertificateRepository;
import com.org.careerbuilder.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public CertificateGroupResponse getCertificates(Long studentId, String academicYear, String search) {
        studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        String searchTerm = (search != null && search.isBlank()) ? null : search;
        String yearFilter = (academicYear != null && academicYear.isBlank()) ? null : academicYear;

        List<CertificateResponse> academic = certificateRepository
                .findByStudentAndCategory(studentId, CertificateCategory.ACADEMIC, yearFilter, searchTerm)
                .stream()
                .map(this::toResponse)
                .toList();

        List<CertificateResponse> miscellaneous = certificateRepository
                .findByStudentAndCategory(studentId, CertificateCategory.MISCELLANEOUS, yearFilter, searchTerm)
                .stream()
                .map(this::toResponse)
                .toList();

        List<String> availableYears = certificateRepository
                .findDistinctAcademicYearsByStudentId(studentId);

        return new CertificateGroupResponse(academic, miscellaneous, availableYears);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadCertificate(Long certificateId, Long studentId) {
        Certificate certificate = certificateRepository.findByIdAndStudentId(certificateId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certificate not found with id: " + certificateId + " for student: " + studentId));

        if (certificate.getStatus() == Certificate.CertificateStatus.UNAVAILABLE) {
            throw new IllegalStateException("Certificate is not yet available for download.");
        }

        if (certificate.getFilePath() == null || certificate.getFilePath().isBlank()) {
            throw new ResourceNotFoundException("No file is attached to certificate: " + certificateId);
        }

        Path path = Paths.get(certificate.getFilePath());
        Resource resource = new PathResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Certificate file does not exist on server.");
        }

        return resource;
    }

    private CertificateResponse toResponse(Certificate c) {
        return new CertificateResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getCategory() != null ? c.getCategory().name() : null,
                c.getStatus() != null ? c.getStatus().name() : null,
                c.getIssuedOn(),
                c.getAcademicYear()
        );
    }
}
