package com.org.careerbuilder.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
