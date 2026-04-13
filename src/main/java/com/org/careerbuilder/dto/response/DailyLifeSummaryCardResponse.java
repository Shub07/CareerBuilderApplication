package com.org.careerbuilder.dto.response;

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
public class DailyLifeSummaryCardResponse {
    private String key;
    private String label;
    private String color;
    private Integer totalMinutes;
    private String hoursText;
}