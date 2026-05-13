package com.org.careerbuilder.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherClassAnnouncementRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotEmpty
    @Valid
    private List<ClassTarget> classes;

    private String attachmentUrl;
    private String attachmentFileName;
    private String attachmentFileType;
    private Long attachmentFileSize;
    private boolean draft;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassTarget {
        @NotBlank
        private String className;
        @NotBlank
        private String section;
    }
}
