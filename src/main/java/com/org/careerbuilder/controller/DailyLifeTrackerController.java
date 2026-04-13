package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.DailyLifeActivityRequest;
import com.org.careerbuilder.dto.request.DailyLifeQuickCheckRequest;
import com.org.careerbuilder.dto.response.DailyLifeActivityResponse;
import com.org.careerbuilder.dto.response.DailyLifeDashboardResponse;
import com.org.careerbuilder.dto.response.DailyLifeNotificationResponse;
import com.org.careerbuilder.dto.response.DailyLifeQuickCheckItemResponse;
import com.org.careerbuilder.service.DailyLifeTrackerService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/daily-life-tracker")
@RequiredArgsConstructor
public class DailyLifeTrackerController {

    private final DailyLifeTrackerService dailyLifeTrackerService;

    @PostMapping("/activities")
    public ResponseEntity<Map<String, Object>> createActivity(@Valid @RequestBody DailyLifeActivityRequest request) {
        DailyLifeActivityResponse response = dailyLifeTrackerService.createActivity(request);
        return buildResponse(HttpStatus.CREATED, true, "Activity created successfully", response);
    }

    @PutMapping("/activities/{activityId}")
    public ResponseEntity<Map<String, Object>> updateActivity(
            @PathVariable Long activityId,
            @Valid @RequestBody DailyLifeActivityRequest request
    ) {
        DailyLifeActivityResponse response = dailyLifeTrackerService.updateActivity(activityId, request);
        return buildResponse(HttpStatus.OK, true, "Activity updated successfully", response);
    }

    @PatchMapping("/activities/{activityId}/completion")
    public ResponseEntity<Map<String, Object>> updateActivityCompletion(
            @PathVariable Long activityId,
            @RequestParam boolean completed
    ) {
        DailyLifeActivityResponse response = dailyLifeTrackerService.updateActivityCompletion(activityId, completed);
        return buildResponse(HttpStatus.OK, true, "Activity completion updated successfully", response);
    }

    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Map<String, Object>> deleteActivity(@PathVariable Long activityId) {
        dailyLifeTrackerService.deleteActivity(activityId);
        return buildResponse(HttpStatus.OK, true, "Activity deleted successfully", null);
    }

    @GetMapping("/students/{studentId}/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String viewerRole,
            @RequestParam(required = false) Long viewerId
    ) {
        DailyLifeDashboardResponse response = dailyLifeTrackerService.getDashboard(studentId, date, viewerRole, viewerId);
        return buildResponse(HttpStatus.OK, true, "Daily life tracker dashboard fetched successfully", response);
    }

    @GetMapping("/students/{studentId}/activities")
    public ResponseEntity<Map<String, Object>> getActivities(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String viewerRole,
            @RequestParam(required = false) Long viewerId
    ) {
        List<DailyLifeActivityResponse> response = dailyLifeTrackerService.getActivities(studentId, date, viewerRole, viewerId);
        return buildResponse(HttpStatus.OK, true, "Daily life activities fetched successfully", response);
    }

    @GetMapping("/students/{studentId}/activities/search")
    public ResponseEntity<Map<String, Object>> searchActivities(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String query,
            @RequestParam(required = false) String viewerRole,
            @RequestParam(required = false) Long viewerId
    ) {
        List<DailyLifeActivityResponse> response = dailyLifeTrackerService.searchActivities(studentId, date, query, viewerRole, viewerId);
        return buildResponse(HttpStatus.OK, true, "Daily life activities searched successfully", response);
    }

    @GetMapping("/students/{studentId}/quick-check")
    public ResponseEntity<Map<String, Object>> getQuickChecks(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String viewerRole,
            @RequestParam(required = false) Long viewerId
    ) {
        List<DailyLifeQuickCheckItemResponse> response = dailyLifeTrackerService.getQuickChecks(studentId, date, viewerRole, viewerId);
        return buildResponse(HttpStatus.OK, true, "Quick checks fetched successfully", response);
    }

    @PutMapping("/students/{studentId}/quick-check")
    public ResponseEntity<Map<String, Object>> updateQuickChecks(
            @PathVariable Long studentId,
            @Valid @RequestBody DailyLifeQuickCheckRequest request
    ) {
        if (!studentId.equals(request.getStudentId())) {
            throw new IllegalArgumentException("Student ID mismatch in quick check update");
        }
        List<DailyLifeQuickCheckItemResponse> response = dailyLifeTrackerService.updateQuickChecks(request);
        return buildResponse(HttpStatus.OK, true, "Quick checks updated successfully", response);
    }

    @GetMapping("/students/{studentId}/notifications")
    public ResponseEntity<Map<String, Object>> getNotifications(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String viewerRole,
            @RequestParam(required = false) Long viewerId
    ) {
        List<DailyLifeNotificationResponse> response = dailyLifeTrackerService.getNotifications(studentId, date, viewerRole, viewerId);
        return buildResponse(HttpStatus.OK, true, "Notifications fetched successfully", response);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            boolean success,
            String message,
            Object data
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", success);
        body.put("message", message);
        if (data != null) {
            body.put("data", data);
        }
        return ResponseEntity.status(status).body(body);
    }
}