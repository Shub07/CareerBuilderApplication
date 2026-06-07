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
public class ExamMarkEntryRowResponse {
    private Long resultId;
    private Long studentId;
    private Integer rollNo;
    private String studentName;
    private Double marks;
    private String remark;
    /** PASS, FAIL, PENDING */
    private String status;
}
