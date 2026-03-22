package com.org.careerbuilder.dto.response;

public record FeeCardResponse(
        Long feeId,
        String feeType,
        String term,
        Double amount,
        String status,
        String dueDate
) {
}
