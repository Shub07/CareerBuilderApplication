package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.*;

/**
 * 📢 Notice Request DTO
 * Request object for creating/updating notices
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeRequest {

    @NotBlank(message = "Notice title is required")
    private String title;

    @NotBlank(message = "Notice description is required")
    private String description;

    @NotNull(message = "Notice category is required")
    private String category;

    private String source;

    private String body;

    private Boolean isPinned;

    private List<String> attachmentUrls;
}
