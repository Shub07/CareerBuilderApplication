package com.org.careerbuilder.dto.dashboard;

import lombok.*;

import java.time.LocalDate;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class NoticeItem {

    private Long id;
    private String title;
    private LocalDate date;
    private boolean unread;
}
