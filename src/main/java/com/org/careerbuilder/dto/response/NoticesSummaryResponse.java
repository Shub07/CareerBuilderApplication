package com.org.careerbuilder.dto.response;

import java.util.List;
import lombok.*;

/**
 * 📊 Notices Summary Response DTO
 * Contains summary cards data and notices grouped by category
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticesSummaryResponse {

    private Long totalNotices;

    private Long newNoticesCount;

    private Long pinnedNoticesCount;

    private List<NoticeResponse> pinnedNotices;

    private List<NoticeResponse> recentNotices;

    private List<NoticeResponse> allNotices;

    private List<CategoryNoticesResponse> categorizedNotices;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryNoticesResponse {
        private String category;
        private String color;
        private String background;
        private Long count;
        private List<NoticeResponse> notices;
    }
}
