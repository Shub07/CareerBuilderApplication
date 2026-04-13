package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.DailyLifeActivityRequest;
import com.org.careerbuilder.dto.request.DailyLifeQuickCheckRequest;
import com.org.careerbuilder.dto.response.DailyLifeActivityResponse;
import com.org.careerbuilder.dto.response.DailyLifeDashboardResponse;
import com.org.careerbuilder.dto.response.DailyLifeNotificationResponse;
import com.org.careerbuilder.dto.response.DailyLifeQuickCheckItemResponse;
import java.time.LocalDate;
import java.util.List;

public interface DailyLifeTrackerService {

    DailyLifeActivityResponse createActivity(DailyLifeActivityRequest request);

    DailyLifeActivityResponse updateActivity(Long activityId, DailyLifeActivityRequest request);

    DailyLifeActivityResponse updateActivityCompletion(Long activityId, boolean completed);

    void deleteActivity(Long activityId);

    DailyLifeDashboardResponse getDashboard(Long studentId, LocalDate date, String viewerRole, Long viewerId);

    List<DailyLifeActivityResponse> getActivities(Long studentId, LocalDate date, String viewerRole, Long viewerId);

    List<DailyLifeActivityResponse> searchActivities(Long studentId, LocalDate date, String query, String viewerRole, Long viewerId);

    List<DailyLifeQuickCheckItemResponse> updateQuickChecks(DailyLifeQuickCheckRequest request);

    List<DailyLifeQuickCheckItemResponse> getQuickChecks(Long studentId, LocalDate date, String viewerRole, Long viewerId);

    List<DailyLifeNotificationResponse> getNotifications(Long studentId, LocalDate date, String viewerRole, Long viewerId);
}