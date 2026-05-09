package com.org.careerbuilder.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportMarksPreviewResponse {
    private Integer totalRows;
    private Integer validRows;
    private Integer invalidRows;
    private List<ImportedMarkRow> rows;
    private List<String> errors;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportedMarkRow {
        private Integer lineNumber;
        private Integer rollNo;
        private String studentName;
        private Double marks;
        private String remark;
        private Boolean isValid;
        private String errorMessage;
    }
}

