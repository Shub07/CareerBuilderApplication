package com.org.careerbuilder.dto.response;

import java.time.LocalDate;

public record CertificateResponse(
        Long id,
        String name,
        String description,
        String category,
        String status,
        LocalDate issuedOn,
        String academicYear
) {}
