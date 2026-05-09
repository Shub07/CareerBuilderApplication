package com.org.careerbuilder.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamMarkEntryRowResponse {
    private Long studentId;
    private Integer rollNo;
    private String studentName;
    private Double marks;
    private String remark;
    private String status; // PASS, FAIL
}

