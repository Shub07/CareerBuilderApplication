package com.org.careerbuilder.dto.dashboard;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentHeader {
    private Long id;
    private String name;
    private String className;
    private String avatarUrl;
}
