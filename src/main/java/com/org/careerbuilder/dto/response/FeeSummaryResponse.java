package com.org.careerbuilder.dto.response;

public record FeeSummaryResponse(
        Double totalFees,
        Double paidAmount,
        Double pendingAmount

) {
}
