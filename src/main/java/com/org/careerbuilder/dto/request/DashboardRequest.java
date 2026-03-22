package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DashboardRequest(
        @NotNull Long studentId,
        LocalDate date // optional; if null -> today
) {}
