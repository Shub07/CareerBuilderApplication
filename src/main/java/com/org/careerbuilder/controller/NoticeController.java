package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.NoticeRequest;
import com.org.careerbuilder.dto.response.NoticeResponse;
import com.org.careerbuilder.dto.response.NoticesSummaryResponse;
import com.org.careerbuilder.models.enums.NoticeCategory;
import com.org.careerbuilder.service.NoticeService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 📢 Notice Controller - Manages all notice operations
 * Endpoints for retrieving, managing, and tracking notices
 */
@Slf4j
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // ===== NOTICE RETRIEVAL OPERATIONS =====

    /**
     * Get comprehensive notices summary with all data
     * GET /api/notices/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getNoticesSummary(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long studentId
    ) {
        log.info("Fetching notices summary for school: {}", schoolId);

        try {
            NoticesSummaryResponse response = noticeService.getNoticesSummary(schoolId, studentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Notices summary retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching notices summary: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get all notices with pagination
     * GET /api/notices/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllNotices(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Fetching all notices for school: {}", schoolId);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NoticeResponse> response = noticeService.getAllNotices(schoolId, studentId, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Notices retrieved successfully");
            result.put("data", response.getContent());
            result.put("totalElements", response.getTotalElements());
            result.put("totalPages", response.getTotalPages());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching notices: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get notices by category
     * GET /api/notices/category
     */
    @GetMapping("/category")
    public ResponseEntity<Map<String, Object>> getNoticesByCategory(
            @RequestParam Long schoolId,
            @RequestParam String category,
            @RequestParam(required = false) Long studentId
    ) {
        log.info("Fetching notices for category: {} in school: {}", category, schoolId);

        try {
            NoticeCategory cat = NoticeCategory.fromValue(category);
            List<NoticeResponse> response = noticeService.getNoticesByCategory(schoolId, studentId, cat);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Category notices retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching category notices: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get pinned notices
     * GET /api/notices/pinned
     */
    @GetMapping("/pinned")
    public ResponseEntity<Map<String, Object>> getPinnedNotices(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long studentId
    ) {
        log.info("Fetching pinned notices for school: {}", schoolId);

        try {
            List<NoticeResponse> response = noticeService.getPinnedNotices(schoolId, studentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Pinned notices retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching pinned notices: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get new notices (created in last 24 hours)
     * GET /api/notices/new
     */
    @GetMapping("/new")
    public ResponseEntity<Map<String, Object>> getNewNotices(
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long studentId
    ) {
        log.info("Fetching new notices for school: {}", schoolId);

        try {
            List<NoticeResponse> response = noticeService.getNewNotices(schoolId, studentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "New notices retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching new notices: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Search notices
     * GET /api/notices/search
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchNotices(
            @RequestParam Long schoolId,
            @RequestParam String query,
            @RequestParam(required = false) Long studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Searching notices in school: {} with query: {}", schoolId, query);

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NoticeResponse> response = noticeService.searchNotices(schoolId, studentId, query, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Search completed successfully");
            result.put("data", response.getContent());
            result.put("totalElements", response.getTotalElements());
            result.put("totalPages", response.getTotalPages());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error searching notices: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get single notice by ID
     * GET /api/notices/{noticeId}
     */
    @GetMapping("/{noticeId}")
    public ResponseEntity<Map<String, Object>> getNoticeById(
            @PathVariable Long noticeId,
            @RequestParam Long schoolId,
            @RequestParam(required = false) Long studentId
    ) {
        log.info("Fetching notice: {} for school: {}", noticeId, schoolId);

        try {
            NoticeResponse response = noticeService.getNoticeById(noticeId, studentId, schoolId);

            // Mark as read
            if (studentId != null) {
                noticeService.markNoticeAsRead(noticeId, studentId);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Notice retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching notice: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Mark notice as read
     * POST /api/notices/{noticeId}/read
     */
    @PostMapping("/{noticeId}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long noticeId,
            @RequestParam Long studentId
    ) {
        log.info("Marking notice: {} as read by student: {}", noticeId, studentId);

        try {
            noticeService.markNoticeAsRead(noticeId, studentId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Notice marked as read");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error marking notice as read: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ===== ADMIN OPERATIONS =====

    /**
     * Create new notice (admin only)
     * POST /api/notices
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotice(
            @RequestParam Long schoolId,
            @Valid @RequestBody NoticeRequest request
    ) {
        log.info("Creating new notice for school: {}", schoolId);

        try {
            NoticeResponse response = noticeService.createNotice(schoolId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Notice created successfully");
            result.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            log.error("Error creating notice: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update notice (admin only)
     * PUT /api/notices/{noticeId}
     */
    @PutMapping("/{noticeId}")
    public ResponseEntity<Map<String, Object>> updateNotice(
            @PathVariable Long noticeId,
            @RequestParam Long schoolId,
            @Valid @RequestBody NoticeRequest request
    ) {
        log.info("Updating notice: {} for school: {}", noticeId, schoolId);

        try {
            NoticeResponse response = noticeService.updateNotice(noticeId, schoolId, request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Notice updated successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error updating notice: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete notice (admin only)
     * DELETE /api/notices/{noticeId}
     */
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Map<String, Object>> deleteNotice(
            @PathVariable Long noticeId,
            @RequestParam Long schoolId
    ) {
        log.info("Deleting notice: {} from school: {}", noticeId, schoolId);

        try {
            noticeService.deleteNotice(noticeId, schoolId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Notice deleted successfully");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error deleting notice: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Toggle pin status of notice
     * PATCH /api/notices/{noticeId}/pin
     */
    @PatchMapping("/{noticeId}/pin")
    public ResponseEntity<Map<String, Object>> togglePin(
            @PathVariable Long noticeId,
            @RequestParam Long schoolId
    ) {
        log.info("Toggling pin status for notice: {} in school: {}", noticeId, schoolId);

        try {
            noticeService.togglePinNotice(noticeId, schoolId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Pin status toggled successfully");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error toggling pin status: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
