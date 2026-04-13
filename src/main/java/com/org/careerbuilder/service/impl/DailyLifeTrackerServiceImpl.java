package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.request.DailyLifeActivityRequest;
import com.org.careerbuilder.dto.request.DailyLifeQuickCheckRequest;
import com.org.careerbuilder.dto.response.DailyLifeActivityResponse;
import com.org.careerbuilder.dto.response.DailyLifeDashboardResponse;
import com.org.careerbuilder.dto.response.DailyLifeNotificationResponse;
import com.org.careerbuilder.dto.response.DailyLifeQuickCheckItemResponse;
import com.org.careerbuilder.dto.response.DailyLifeSummaryCardResponse;
import com.org.careerbuilder.dto.response.DailyLifeTimelineBlockResponse;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.AppUser;
import com.org.careerbuilder.models.DailyLifeActivity;
import com.org.careerbuilder.models.DailyLifeQuickCheck;
import com.org.careerbuilder.models.Parent;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.enums.DailyLifeActivityType;
import com.org.careerbuilder.models.enums.UserRole;
import com.org.careerbuilder.repository.AppUserRepository;
import com.org.careerbuilder.repository.DailyLifeActivityRepository;
import com.org.careerbuilder.repository.DailyLifeQuickCheckRepository;
import com.org.careerbuilder.repository.ParentRepository;
import com.org.careerbuilder.service.DailyLifeTrackerService;
import com.org.careerbuilder.service.StudentLookupService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DailyLifeTrackerServiceImpl implements DailyLifeTrackerService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final List<QuickCheckTemplate> QUICK_CHECK_TEMPLATES = List.of(
            new QuickCheckTemplate("attended-school", "Attended school today"),
            new QuickCheckTemplate("completed-homework", "Completed homework"),
            new QuickCheckTemplate("physical-activity", "Did physical activity"),
            new QuickCheckTemplate("screen-time-balanced", "Maintained balanced screen time"),
            new QuickCheckTemplate("healthy-meals", "Had healthy meals"),
            new QuickCheckTemplate("sufficient-rest", "Took sufficient rest")
    );

    private final DailyLifeActivityRepository activityRepository;
    private final DailyLifeQuickCheckRepository quickCheckRepository;
    private final StudentLookupService studentLookupService;
    private final ParentRepository parentRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public DailyLifeActivityResponse createActivity(DailyLifeActivityRequest request) {
        Student student = studentLookupService.getStudentOrThrow(request.getStudentId());
        validateActivityRequest(request);

        DailyLifeActivity activity = DailyLifeActivity.builder()
                .student(student)
                .activityDate(request.getActivityDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .activityType(DailyLifeActivityType.fromValue(request.getActivityType()))
                .note(trimToNull(request.getNote()))
                .completed(Boolean.TRUE.equals(request.getCompleted()))
                .build();

        return toActivityResponse(activityRepository.save(activity));
    }

    @Override
    public DailyLifeActivityResponse updateActivity(Long activityId, DailyLifeActivityRequest request) {
        DailyLifeActivity activity = getActivityOrThrow(activityId);
        validateActivityRequest(request);

        if (!activity.getStudent().getId().equals(request.getStudentId())) {
            throw new IllegalArgumentException("Student ID mismatch for activity update");
        }

        activity.setActivityDate(request.getActivityDate());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setActivityType(DailyLifeActivityType.fromValue(request.getActivityType()));
        activity.setNote(trimToNull(request.getNote()));
        activity.setCompleted(Boolean.TRUE.equals(request.getCompleted()));

        return toActivityResponse(activityRepository.save(activity));
    }

    @Override
    public DailyLifeActivityResponse updateActivityCompletion(Long activityId, boolean completed) {
        DailyLifeActivity activity = getActivityOrThrow(activityId);
        activity.setCompleted(completed);
        return toActivityResponse(activityRepository.save(activity));
    }

    @Override
    public void deleteActivity(Long activityId) {
        DailyLifeActivity activity = getActivityOrThrow(activityId);
        activityRepository.delete(activity);
    }

    @Override
    public DailyLifeDashboardResponse getDashboard(Long studentId, LocalDate date, String viewerRole, Long viewerId) {
        Student student = validateViewerAccess(studentId, viewerRole, viewerId);
        LocalDate selectedDate = resolveDate(date);
        List<DailyLifeActivity> activities = activityRepository
                .findByStudent_IdAndActivityDateOrderByStartTimeAsc(studentId, selectedDate);
        List<DailyLifeQuickCheck> quickChecks = getOrCreateQuickChecks(student, selectedDate);

        int totalActivities = activities.size();
        int completedActivities = (int) activities.stream().filter(DailyLifeActivity::isCompleted).count();
        int completionRate = totalActivities == 0 ? 0 : (completedActivities * 100) / totalActivities;

        return DailyLifeDashboardResponse.builder()
                .studentId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .selectedDate(selectedDate)
                .totalActivities(totalActivities)
                .completedActivities(completedActivities)
                .completionRate(completionRate)
                .summaryCards(buildSummaryCards(activities))
                .activities(activities.stream().map(this::toActivityResponse).toList())
                .timeline(buildTimeline(activities))
                .quickChecks(quickChecks.stream().map(this::toQuickCheckResponse).toList())
                .notifications(buildNotifications(activities, quickChecks, student, selectedDate))
                .build();
    }

    @Override
    public List<DailyLifeActivityResponse> getActivities(Long studentId, LocalDate date, String viewerRole, Long viewerId) {
        validateViewerAccess(studentId, viewerRole, viewerId);
        return activityRepository.findByStudent_IdAndActivityDateOrderByStartTimeAsc(studentId, resolveDate(date))
                .stream()
                .map(this::toActivityResponse)
                .toList();
    }

    @Override
    public List<DailyLifeActivityResponse> searchActivities(Long studentId, LocalDate date, String query, String viewerRole, Long viewerId) {
        validateViewerAccess(studentId, viewerRole, viewerId);
        if (query == null || query.isBlank()) {
            return getActivities(studentId, date, viewerRole, viewerId);
        }

        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        return activityRepository.findByStudent_IdAndActivityDateOrderByStartTimeAsc(studentId, resolveDate(date))
                .stream()
                .filter(activity -> matchesSearch(activity, normalizedQuery))
                .map(this::toActivityResponse)
                .toList();
    }

    @Override
    public List<DailyLifeQuickCheckItemResponse> updateQuickChecks(DailyLifeQuickCheckRequest request) {
        Student student = studentLookupService.getStudentOrThrow(request.getStudentId());
        LocalDate selectedDate = resolveDate(request.getDate());

        Map<String, DailyLifeQuickCheck> existingByKey = getOrCreateQuickChecks(student, selectedDate)
                .stream()
                .collect(Collectors.toMap(DailyLifeQuickCheck::getCheckKey, item -> item, (left, right) -> left, LinkedHashMap::new));

        for (DailyLifeQuickCheckRequest.QuickCheckItem item : request.getItems()) {
            DailyLifeQuickCheck quickCheck = existingByKey.get(item.getCheckKey());
            if (quickCheck == null) {
                quickCheck = DailyLifeQuickCheck.builder()
                        .student(student)
                        .checkDate(selectedDate)
                        .checkKey(item.getCheckKey())
                        .label(item.getLabel())
                        .completed(item.isCompleted())
                        .build();
            } else {
                quickCheck.setLabel(item.getLabel());
                quickCheck.setCompleted(item.isCompleted());
            }
            existingByKey.put(item.getCheckKey(), quickCheckRepository.save(quickCheck));
        }

        return existingByKey.values().stream()
                .sorted(Comparator.comparing(DailyLifeQuickCheck::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toQuickCheckResponse)
                .toList();
    }

    @Override
    public List<DailyLifeQuickCheckItemResponse> getQuickChecks(Long studentId, LocalDate date, String viewerRole, Long viewerId) {
        Student student = validateViewerAccess(studentId, viewerRole, viewerId);
        return getOrCreateQuickChecks(student, resolveDate(date)).stream()
                .map(this::toQuickCheckResponse)
                .toList();
    }

    @Override
    public List<DailyLifeNotificationResponse> getNotifications(Long studentId, LocalDate date, String viewerRole, Long viewerId) {
        Student student = validateViewerAccess(studentId, viewerRole, viewerId);
        LocalDate selectedDate = resolveDate(date);
        List<DailyLifeActivity> activities = activityRepository.findByStudent_IdAndActivityDateOrderByStartTimeAsc(studentId, selectedDate);
        List<DailyLifeQuickCheck> quickChecks = getOrCreateQuickChecks(student, selectedDate);
        return buildNotifications(activities, quickChecks, student, selectedDate);
    }

    private Student validateViewerAccess(Long studentId, String viewerRole, Long viewerId) {
        Student student = studentLookupService.getStudentOrThrow(studentId);

        if (viewerRole == null || viewerRole.isBlank()) {
            return student;
        }

        String normalizedRole = viewerRole.trim().toUpperCase(Locale.ROOT);
        switch (normalizedRole) {
            case "STUDENT" -> {
                if (viewerId == null || !student.getId().equals(viewerId)) {
                    throw new IllegalArgumentException("Student can only access own daily life tracker");
                }
            }
            case "PARENT" -> {
                if (viewerId == null) {
                    throw new IllegalArgumentException("Parent viewer ID is required");
                }
                Parent parent = parentRepository.findById(viewerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Parent not found with ID: " + viewerId));
                if (parent.getStudent() == null || !student.getId().equals(parent.getStudent().getId())) {
                    throw new IllegalArgumentException("Parent is not linked to this student");
                }
            }
            case "ADMIN" -> {
                if (viewerId == null) {
                    throw new IllegalArgumentException("Admin viewer ID is required");
                }
                AppUser appUser = appUserRepository.findById(viewerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with ID: " + viewerId));
                if (appUser.getRole() != UserRole.ADMIN) {
                    throw new IllegalArgumentException("Viewer is not authorized as admin");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported viewer role: " + viewerRole);
        }

        return student;
    }

    private void validateActivityRequest(DailyLifeActivityRequest request) {
        if (request.getEndTime().equals(request.getStartTime())) {
            throw new IllegalArgumentException("Start time and end time cannot be the same");
        }
    }

    private LocalDate resolveDate(LocalDate date) {
        return date != null ? date : LocalDate.now();
    }

    private DailyLifeActivity getActivityOrThrow(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Daily life activity not found with ID: " + activityId));
    }

    private List<DailyLifeQuickCheck> getOrCreateQuickChecks(Student student, LocalDate date) {
        List<DailyLifeQuickCheck> existing = quickCheckRepository.findByStudent_IdAndCheckDateOrderByIdAsc(student.getId(), date);
        if (!existing.isEmpty()) {
            return existing;
        }

        List<DailyLifeQuickCheck> created = QUICK_CHECK_TEMPLATES.stream()
                .map(template -> DailyLifeQuickCheck.builder()
                        .student(student)
                        .checkDate(date)
                        .checkKey(template.key())
                        .label(template.label())
                        .completed(false)
                        .build())
                .map(quickCheckRepository::save)
                .toList();
        return new ArrayList<>(created);
    }

    private List<DailyLifeSummaryCardResponse> buildSummaryCards(List<DailyLifeActivity> activities) {
        Map<DailyLifeActivityType, Integer> totals = Arrays.stream(DailyLifeActivityType.values())
                .collect(Collectors.toMap(type -> type, type -> 0, (left, right) -> left, LinkedHashMap::new));

        for (DailyLifeActivity activity : activities) {
            totals.computeIfPresent(activity.getActivityType(), (key, value) -> value + activity.getDurationMinutes());
        }

        return totals.entrySet().stream()
                .map(entry -> DailyLifeSummaryCardResponse.builder()
                        .key(entry.getKey().name())
                        .label(entry.getKey().getLabel())
                        .color(entry.getKey().getColor())
                        .totalMinutes(entry.getValue())
                        .hoursText(formatHours(entry.getValue()))
                        .build())
                .toList();
    }

    private List<DailyLifeTimelineBlockResponse> buildTimeline(List<DailyLifeActivity> activities) {
        List<DailyLifeTimelineBlockResponse> timeline = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            final int currentHour = hour;
            DailyLifeActivity matching = activities.stream()
                    .filter(activity -> isHourCovered(activity, currentHour))
                    .findFirst()
                    .orElse(null);

            timeline.add(DailyLifeTimelineBlockResponse.builder()
                    .hour(hour)
                    .label(matching != null ? matching.getActivityType().getLabel() : null)
                    .time(matching != null ? formatTimeRange(matching.getStartTime(), matching.getEndTime()) : null)
                    .color(matching != null ? matching.getActivityType().getColor() : "#f0f0f0")
                    .active(matching != null)
                    .build());
        }
        return timeline;
    }

    private boolean isHourCovered(DailyLifeActivity activity, int hour) {
        int startHour = activity.getStartTime().getHour();
        int endHour = activity.getEndTime().equals(LocalTime.MIDNIGHT) ? 24 : activity.getEndTime().getHour();
        if (activity.getEndTime().isBefore(activity.getStartTime())) {
            return hour >= startHour || hour < endHour;
        }
        return hour >= startHour && hour < Math.max(endHour, startHour + 1);
    }

    private boolean matchesSearch(DailyLifeActivity activity, String normalizedQuery) {
        return activity.getActivityType().getLabel().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                || activity.getActivityType().name().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                || (activity.getNote() != null && activity.getNote().toLowerCase(Locale.ROOT).contains(normalizedQuery));
    }

    private List<DailyLifeNotificationResponse> buildNotifications(
            List<DailyLifeActivity> activities,
            List<DailyLifeQuickCheck> quickChecks,
            Student student,
            LocalDate date
    ) {
        List<DailyLifeNotificationResponse> notifications = new ArrayList<>();

        long incompleteActivities = activities.stream().filter(activity -> !activity.isCompleted()).count();
        if (incompleteActivities > 0) {
            notifications.add(DailyLifeNotificationResponse.builder()
                    .type("warning")
                    .title("Pending activities")
                    .message(incompleteActivities + " activity entries are not marked completed for " + date)
                    .build());
        }

        long missingQuickChecks = quickChecks.stream().filter(item -> !item.isCompleted()).count();
        if (missingQuickChecks > 0) {
            notifications.add(DailyLifeNotificationResponse.builder()
                    .type("info")
                    .title("Quick check pending")
                    .message(missingQuickChecks + " quick checks are still unchecked for " + student.getFirstName())
                    .build());
        }

        if (activities.isEmpty()) {
            notifications.add(DailyLifeNotificationResponse.builder()
                    .type("reminder")
                    .title("No daily activity logged")
                    .message("No activities have been logged for this day yet")
                    .build());
        }

        return notifications;
    }

    private DailyLifeActivityResponse toActivityResponse(DailyLifeActivity activity) {
        Student student = activity.getStudent();
        return DailyLifeActivityResponse.builder()
                .id(activity.getId())
                .studentId(student.getId())
                .studentName(student.getFirstName() + " " + student.getLastName())
                .activityDate(activity.getActivityDate())
                .startTime(activity.getStartTime())
                .endTime(activity.getEndTime())
                .activityType(activity.getActivityType().name())
                .activityTypeLabel(activity.getActivityType().getLabel())
                .color(activity.getActivityType().getColor())
                .note(activity.getNote())
                .completed(activity.isCompleted())
                .durationMinutes(activity.getDurationMinutes())
                .durationText(formatHours(activity.getDurationMinutes()))
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }

    private DailyLifeQuickCheckItemResponse toQuickCheckResponse(DailyLifeQuickCheck quickCheck) {
        return DailyLifeQuickCheckItemResponse.builder()
                .checkKey(quickCheck.getCheckKey())
                .label(quickCheck.getLabel())
                .completed(quickCheck.isCompleted())
                .build();
    }

    private String formatHours(int minutes) {
        double hours = minutes / 60.0;
        return String.format(Locale.ENGLISH, "%.1fh", hours);
    }

    private String formatTimeRange(LocalTime startTime, LocalTime endTime) {
        return startTime.format(TIME_FORMATTER) + " - " + endTime.format(TIME_FORMATTER);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record QuickCheckTemplate(String key, String label) {
    }
}