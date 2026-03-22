package com.org.careerbuilder.dto.dashboard;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class TodayScheduleItem {

    private int period;
    private String subject;
    private String teacher;
    private String start;
    private String end;
}
