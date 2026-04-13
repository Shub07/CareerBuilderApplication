package com.org.careerbuilder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyLifeQuickCheckRequest {

    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @Valid
    @NotEmpty(message = "Quick check items are required")
    private List<QuickCheckItem> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuickCheckItem {

        @NotNull(message = "Quick check key is required")
        @Size(min = 2, max = 80)
        private String checkKey;

        @NotNull(message = "Quick check label is required")
        @Size(min = 2, max = 150)
        private String label;

        private boolean completed;
    }
}