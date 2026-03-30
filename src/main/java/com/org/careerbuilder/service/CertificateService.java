package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.CertificateGroupResponse;
import org.springframework.core.io.Resource;

public interface CertificateService {

    /**
     * Returns all certificates for the given student,
     * grouped into Academic and Miscellaneous, with optional filters.
     */
    CertificateGroupResponse getCertificates(Long studentId, String academicYear, String search);

    /**
     * Returns the file resource for the given certificate so it can be streamed as a download.
     */
    Resource downloadCertificate(Long certificateId, Long studentId);
}
