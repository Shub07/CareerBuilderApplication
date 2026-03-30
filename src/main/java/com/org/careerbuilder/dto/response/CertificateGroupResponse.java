package com.org.careerbuilder.dto.response;

import java.util.List;

public record CertificateGroupResponse(
        List<CertificateResponse> academicCertificates,
        List<CertificateResponse> miscellaneousCertificates,
        List<String> availableAcademicYears
) {}
