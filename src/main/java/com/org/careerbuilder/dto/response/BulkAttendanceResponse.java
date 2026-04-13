package com.org.careerbuilder.dto.response;

import lombok.*;

/**
 * DTO for bulk attendance upload response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkAttendanceResponse {

    private Long batchId;

    private String className;

    private String section;

    private Integer totalRecords;

    private Integer successfulRecords;

    private Integer failedRecords;

    private String uploadStatus;

    private String remarks;

    private String uploadedAt;
}

