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
public class DailyLifeTimelineBlockResponse {
    private int hour;
    private String label;
    private String time;
    private String color;
    private boolean active;
}