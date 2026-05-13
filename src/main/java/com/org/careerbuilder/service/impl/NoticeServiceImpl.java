package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.NoticeRequest;
import com.org.careerbuilder.dto.response.NoticeResponse;
import com.org.careerbuilder.dto.response.NoticesSummaryResponse;
import com.org.careerbuilder.models.Notice;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.StudentNoticeRead;
import com.org.careerbuilder.models.enums.NoticeCategory;
import com.org.careerbuilder.repository.NoticeRepository;
import com.org.careerbuilder.repository.NoticeAudienceStudentRepository;
import com.org.careerbuilder.repository.StudentNoticeReadRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.service.NoticeService;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ðŸ“¢ Notice Service Implementation
 * Handles all notice-related business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeAudienceStudentRepository noticeAudienceStudentRepository;
    private final StudentNoticeReadRepository studentNoticeReadRepository;
    private final StudentRepository studentRepository;

    private static final int NEW_NOTICE_HOURS = 24; // Notice is "new" if created within 24 hours

    /**
     * Get comprehensive notices summary with all data for dashboard
     */
    @Override
    @Transactional(readOnly = true)
    public NoticesSummaryResponse getNoticesSummary(Long schoolId, Long studentId) {
        log.info("Fetching notices summary for school: {}", schoolId);

        // Get all notices for school
        List<Notice> allNoticesRaw = noticeRepository.findBySchoolIdOrderByIsPinnedDescCreatedAtDesc(schoolId, Pageable.unpaged()).getContent();
        List<Notice> allNotices = filterVisibleNotices(allNoticesRaw, studentId);

        // Calculate new notices threshold (24 hours ago)
        LocalDateTime newNoticesThreshold = LocalDateTime.now().minusHours(NEW_NOTICE_HOURS);

        // Filter notices
        List<Notice> pinnedNotices = allNotices.stream()
                .filter(Notice::getIsPinned)
                .toList();

        List<Notice> newNoticesList = allNotices.stream()
                .filter(n -> n.getCreatedAt().isAfter(newNoticesThreshold))
                .toList();

        List<Notice> recentNotices = allNotices.stream()
                .limit(10)
                .toList();

        // Convert to responses
        List<NoticeResponse> pinnedResponses = pinnedNotices.stream()
                .map(n -> toNoticeResponse(n, studentId))
                .toList();

        List<NoticeResponse> recentResponses = recentNotices.stream()
                .map(n -> toNoticeResponse(n, studentId))
                .toList();

        // Build categorized notices
        List<NoticesSummaryResponse.CategoryNoticesResponse> categorizedNotices = Arrays.stream(NoticeCategory.values())
                .map(category -> {
                    List<Notice> categoryNotices = allNotices.stream()
                            .filter(n -> n.getCategory() == category)
                            .toList();

                    return NoticesSummaryResponse.CategoryNoticesResponse.builder()
                            .category(category.getLabel())
                            .color(category.getColor())
                            .background(category.getBackground())
                            .count((long) categoryNotices.size())
                            .notices(categoryNotices.stream()
                                    .map(n -> toNoticeResponse(n, studentId))
                                    .toList())
                            .build();
                })
                .filter(c -> c.getCount() > 0)
                .toList();

        return NoticesSummaryResponse.builder()
                .totalNotices((long) allNotices.size())
                .newNoticesCount((long) newNoticesList.size())
                .pinnedNoticesCount((long) pinnedNotices.size())
                .pinnedNotices(pinnedResponses)
                .recentNotices(recentResponses)
                .allNotices(allNotices.stream()
                        .map(n -> toNoticeResponse(n, studentId))
                        .toList())
                .categorizedNotices(categorizedNotices)
                .build();
    }

    /**
     * Get all notices with pagination
     */
    @Override
    @Transactional(readOnly = true)
    public Page<NoticeResponse> getAllNotices(Long schoolId, Long studentId, Pageable pageable) {
        log.info("Fetching all notices for school: {}", schoolId);

        Page<Notice> noticePage = noticeRepository.findBySchoolIdOrderByIsPinnedDescCreatedAtDesc(schoolId, pageable);
        if (studentId == null) {
            return noticePage.map(n -> toNoticeResponse(n, studentId));
        }
        List<Notice> visible = filterVisibleNotices(
                noticeRepository.findBySchoolIdOrderByIsPinnedDescCreatedAtDesc(schoolId, Pageable.unpaged()).getContent(),
                studentId
        );
        return toPage(visible, pageable).map(n -> toNoticeResponse(n, studentId));
    }

    /**
     * Get notices by category
     */
    @Override
    @Transactional(readOnly = true)
    public List<NoticeResponse> getNoticesByCategory(Long schoolId, Long studentId, NoticeCategory category) {
        log.info("Fetching notices for school: {} with category: {}", schoolId, category);

        List<Notice> notices = noticeRepository.findBySchoolIdAndCategoryOrderByIsPinnedDescCreatedAtDesc(schoolId, category);
        notices = filterVisibleNotices(notices, studentId);

        return notices.stream()
                .map(n -> toNoticeResponse(n, studentId))
                .collect(Collectors.toList());
    }

    /**
     * Get pinned notices
     */
    @Override
    @Transactional(readOnly = true)
    public List<NoticeResponse> getPinnedNotices(Long schoolId, Long studentId) {
        log.info("Fetching pinned notices for school: {}", schoolId);

        List<Notice> notices = noticeRepository.findBySchoolIdAndIsPinnedTrueOrderByCreatedAtDesc(schoolId);
        notices = filterVisibleNotices(notices, studentId);

        return notices.stream()
                .map(n -> toNoticeResponse(n, studentId))
                .collect(Collectors.toList());
    }

    /**
     * Get new notices (created in last 24 hours)
     */
    @Override
    @Transactional(readOnly = true)
    public List<NoticeResponse> getNewNotices(Long schoolId, Long studentId) {
        log.info("Fetching new notices for school: {}", schoolId);

        LocalDateTime threshold = LocalDateTime.now().minusHours(NEW_NOTICE_HOURS);
        List<Notice> notices = noticeRepository.findNewNotices(schoolId, threshold);
        notices = filterVisibleNotices(notices, studentId);

        return notices.stream()
                .map(n -> toNoticeResponse(n, studentId))
                .collect(Collectors.toList());
    }

    /**
     * Search notices by query
     */
    @Override
    @Transactional(readOnly = true)
    public Page<NoticeResponse> searchNotices(Long schoolId, Long studentId, String query, Pageable pageable) {
        log.info("Searching notices for school: {} with query: {}", schoolId, query);

        Page<Notice> noticePage = noticeRepository.searchNotices(schoolId, query, pageable);
        if (studentId == null) {
            return noticePage.map(n -> toNoticeResponse(n, studentId));
        }
        List<Notice> visible = filterVisibleNotices(
                noticeRepository.searchNotices(schoolId, query, Pageable.unpaged()).getContent(),
                studentId
        );
        return toPage(visible, pageable).map(n -> toNoticeResponse(n, studentId));
    }

    /**
     * Get single notice by ID
     */
    @Override
    @Transactional(readOnly = true)
    public NoticeResponse getNoticeById(Long noticeId, Long studentId, Long schoolId) {
        log.info("Fetching notice: {} for school: {}", noticeId, schoolId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        if (!notice.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Unauthorized access to notice");
        }
        if (studentId != null && !isVisibleToStudent(notice.getId(), studentId)) {
            throw new RuntimeException("Notice is not visible for this student");
        }

        return toNoticeResponse(notice, studentId);
    }

    /**
     * Mark notice as read by student
     */
    @Override
    public void markNoticeAsRead(Long noticeId, Long studentId) {
        log.info("Marking notice: {} as read by student: {}", noticeId, studentId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if already read
        boolean isRead = studentNoticeReadRepository.existsByStudent_IdAndNotice_Id(studentId, noticeId);

        if (!isRead) {
            StudentNoticeRead read = StudentNoticeRead.builder()
                    .student(student)
                    .notice(notice)
                    .readAt(LocalDateTime.now())
                    .build();

            studentNoticeReadRepository.save(read);
        }
    }

    /**
     * Create new notice
     */
    @Override
    public NoticeResponse createNotice(Long schoolId, NoticeRequest request) {
        log.info("Creating new notice for school: {}", schoolId);

        Notice notice = Notice.builder()
                .schoolId(schoolId)
                .title(request.getTitle())
                .description(request.getDescription())
                .body(request.getBody())
                .category(NoticeCategory.fromValue(request.getCategory()))
                .source(request.getSource())
                .isPinned(request.getIsPinned() != null ? request.getIsPinned() : false)
                .createdAt(LocalDateTime.now())
                .build();

        Notice saved = noticeRepository.save(notice);
        log.info("Notice created with ID: {}", saved.getId());

        return toNoticeResponse(saved, null);
    }

    /**
     * Update notice
     */
    @Override
    public NoticeResponse updateNotice(Long noticeId, Long schoolId, NoticeRequest request) {
        log.info("Updating notice: {} for school: {}", noticeId, schoolId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        if (!notice.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Unauthorized access to notice");
        }

        notice.setTitle(request.getTitle());
        notice.setDescription(request.getDescription());
        notice.setBody(request.getBody());
        notice.setCategory(NoticeCategory.fromValue(request.getCategory()));
        notice.setSource(request.getSource());
        if (request.getIsPinned() != null) {
            notice.setIsPinned(request.getIsPinned());
        }
        notice.setUpdatedAt(LocalDateTime.now());

        Notice updated = noticeRepository.save(notice);
        log.info("Notice updated: {}", noticeId);

        return toNoticeResponse(updated, null);
    }

    /**
     * Delete notice
     */
    @Override
    public void deleteNotice(Long noticeId, Long schoolId) {
        log.info("Deleting notice: {} for school: {}", noticeId, schoolId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        if (!notice.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Unauthorized access to notice");
        }

        noticeRepository.delete(notice);
        log.info("Notice deleted: {}", noticeId);
    }

    /**
     * Toggle pin status of notice
     */
    @Override
    public void togglePinNotice(Long noticeId, Long schoolId) {
        log.info("Toggling pin status for notice: {} in school: {}", noticeId, schoolId);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("Notice not found"));

        if (!notice.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Unauthorized access to notice");
        }

        notice.setIsPinned(!notice.getIsPinned());
        notice.setUpdatedAt(LocalDateTime.now());

        noticeRepository.save(notice);
        log.info("Pin status toggled for notice: {}", noticeId);
    }

    /**
     * Convert Notice entity to NoticeResponse DTO
     */
    private NoticeResponse toNoticeResponse(Notice notice, Long studentId) {
        LocalDateTime newThreshold = LocalDateTime.now().minusHours(NEW_NOTICE_HOURS);
        boolean isNew = notice.getCreatedAt().isAfter(newThreshold);

        boolean isRead = false;
        if (studentId != null) {
            isRead = studentNoticeReadRepository.existsByStudent_IdAndNotice_Id(studentId, notice.getId());
        }

        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .description(notice.getDescription())
                .category(notice.getCategory().getLabel())
                .categoryColor(notice.getCategory().getColor())
                .categoryBackground(notice.getCategory().getBackground())
                .source(notice.getSource())
                .body(notice.getBody())
                .isPinned(notice.getIsPinned())
                .isNew(isNew)
                .isRead(isRead)
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    private List<Notice> filterVisibleNotices(List<Notice> notices, Long studentId) {
        if (studentId == null || notices.isEmpty()) {
            return notices;
        }
        Set<Long> targetedNoticeIds = notices.stream()
                .map(Notice::getId)
                .filter(id -> noticeAudienceStudentRepository.existsByNotice_IdAndStudent_Id(id, studentId))
                .collect(Collectors.toSet());
        return notices.stream()
                .filter(n -> {
                    long targeted = noticeAudienceStudentRepository.countByNotice_Id(n.getId());
                    return targeted == 0 || targetedNoticeIds.contains(n.getId());
                })
                .toList();
    }

    private boolean isVisibleToStudent(Long noticeId, Long studentId) {
        long targeted = noticeAudienceStudentRepository.countByNotice_Id(noticeId);
        return targeted == 0 || noticeAudienceStudentRepository.existsByNotice_IdAndStudent_Id(noticeId, studentId);
    }

    private Page<Notice> toPage(List<Notice> notices, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= notices.size()) {
            return new PageImpl<>(List.of(), pageable, notices.size());
        }
        int end = Math.min(start + pageable.getPageSize(), notices.size());
        return new PageImpl<>(notices.subList(start, end), pageable, notices.size());
    }

        // â”€â”€ Backward-compat methods used by Dashboard services â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

        @Override
        public List<Notice> getRecentNotices(Long schoolId) {
                return noticeRepository.findTop10BySchoolIdOrderByCreatedAtDesc(schoolId);
        }

        @Override
        public int getTotalNoticeCount(Long schoolId) {
                return (int) noticeRepository.countBySchoolId(schoolId);
        }

        @Override
        public int getUnreadNoticeCount(Long studentId, List<Notice> notices) {
                long readCount = notices.stream()
                                .filter(n -> studentNoticeReadRepository.existsByStudent_IdAndNotice_Id(studentId, n.getId()))
                                .count();
                return (int) (notices.size() - readCount);
        }
}
