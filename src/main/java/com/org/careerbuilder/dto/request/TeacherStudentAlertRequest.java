package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherStudentAlertRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotEmpty
    private List<Long> studentIds;

    private String attachmentUrl;
    private String attachmentFileName;
    private String attachmentFileType;
    private Long attachmentFileSize;
    private boolean draft;
}
