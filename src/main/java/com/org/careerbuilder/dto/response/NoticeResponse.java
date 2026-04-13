package com.org.careerbuilder.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

/**
 * 📢 Notice Response DTO
 * Response object for notice data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeResponse {

    private Long id;

    private String title;

    private String description;

    private String category;

    private String categoryColor;

    private String categoryBackground;

    private String source;

    private Boolean isPinned;

    private Boolean isNew;

    private Boolean isRead;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<AttachmentResponse> attachments;

    private String body;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttachmentResponse {
        private Long id;
        private String fileName;
        private String fileType;
        private String fileUrl;
        private Long fileSize;
    }
}
