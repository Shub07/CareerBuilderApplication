package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.NoticeRequest;
import com.org.careerbuilder.dto.response.NoticeResponse;
import com.org.careerbuilder.dto.response.NoticesSummaryResponse;
import com.org.careerbuilder.models.Notice;
import com.org.careerbuilder.models.enums.NoticeCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 📢 Notice Service Interface
 */
public interface NoticeService {

    /**
     * Get all notices summary for school (with summary cards data)
     */
    NoticesSummaryResponse getNoticesSummary(Long schoolId, Long studentId);

    /**
     * Get all notices for school (paginated)
     */
    Page<NoticeResponse> getAllNotices(Long schoolId, Long studentId, Pageable pageable);

    /**
     * Get notices by category
     */
    List<NoticeResponse> getNoticesByCategory(Long schoolId, Long studentId, NoticeCategory category);

    /**
     * Get pinned notices
     */
    List<NoticeResponse> getPinnedNotices(Long schoolId, Long studentId);

    /**
     * Get new notices (created in last 24 hours)
     */
    List<NoticeResponse> getNewNotices(Long schoolId, Long studentId);

    /**
     * Search notices by title or body
     */
    Page<NoticeResponse> searchNotices(Long schoolId, Long studentId, String query, Pageable pageable);

    /**
     * Get single notice by ID
     */
    NoticeResponse getNoticeById(Long noticeId, Long studentId, Long schoolId);

    /**
     * Mark notice as read by student
     */
    void markNoticeAsRead(Long noticeId, Long studentId);

    /**
     * Create new notice (admin only)
     */
    NoticeResponse createNotice(Long schoolId, NoticeRequest request);

    /**
     * Update notice (admin only)
     */
    NoticeResponse updateNotice(Long noticeId, Long schoolId, NoticeRequest request);

    /**
     * Delete notice (admin only)
     */
    void deleteNotice(Long noticeId, Long schoolId);

    /**
     * Pin/unpin notice
     */
    void togglePinNotice(Long noticeId, Long schoolId);

    // ── Backward-compat methods used by Dashboard services ──────────────────────

    /** Get top-10 recent notices for a school (returns entities for dashboard mapping) */
    List<Notice> getRecentNotices(Long schoolId);

    /** Total notice count for a school */
    int getTotalNoticeCount(Long schoolId);

    /** Count of unread notices among the given list for a student */
    int getUnreadNoticeCount(Long studentId, List<Notice> notices);
}
