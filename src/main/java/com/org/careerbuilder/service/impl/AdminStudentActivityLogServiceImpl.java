package com.org.careerbuilder.service.impl;

import com.org.careerbuilder.dto.response.AdminStudentCertificateDtos;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.AdminActivityLog;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.repository.AdminActivityLogRepository;
import com.org.careerbuilder.repository.StudentRepository;
import com.org.careerbuilder.service.AdminStudentActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminStudentActivityLogServiceImpl implements AdminStudentActivityLogService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final StudentRepository studentRepository;
    private final AdminActivityLogRepository activityLogRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStudentCertificateDtos.ActivityLogFeedResponse getActivityLog(
            Long schoolId, Long studentId, String search, String logType, int page, int size) {
        requireStudent(schoolId, studentId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 50);
        String q = (search != null && !search.isBlank()) ? search.trim() : null;
        List<AdminActivityType> typeFilter = resolveActivityTypes(logType);

        Page<AdminActivityLog> result;
        if (typeFilter == null || typeFilter.isEmpty()) {
            result = activityLogRepository.searchStudentActivityLog(
                    schoolId, "STUDENT", studentId, q, null,
                    PageRequest.of(safePage, safeSize));
        } else {
            result = activityLogRepository.searchStudentActivityLogByTypes(
                    schoolId, "STUDENT", studentId, q, typeFilter,
                    PageRequest.of(safePage, safeSize));
        }

        List<AdminStudentCertificateDtos.ActivityLogEntry> entries = result.getContent().stream()
                .map(this::toEntry)
                .toList();

        boolean hasMore = result.hasNext();
        return new AdminStudentCertificateDtos.ActivityLogFeedResponse(
                entries,
                safePage,
                safeSize,
                result.getTotalElements(),
                result.getTotalPages(),
                hasMore
        );
    }

    @Override
    public AdminStudentCertificateDtos.ActivityLogFilterOptions getFilterOptions() {
        return new AdminStudentCertificateDtos.ActivityLogFilterOptions(
                List.of("ALL", "FEE", "ATTENDANCE", "CERTIFICATE", "PROMOTION", "PROFILE", "OTHER")
        );
    }

    private AdminStudentCertificateDtos.ActivityLogEntry toEntry(AdminActivityLog log) {
        String performedBy = log.getPerformedBy() != null ? log.getPerformedBy() : "School Admin";
        return new AdminStudentCertificateDtos.ActivityLogEntry(
                log.getId(),
                mapLogType(log.getActivityType()),
                log.getTitle(),
                log.getDescription(),
                performedBy,
                inferActorRole(performedBy),
                log.getCreatedAt(),
                formatDisplayTimestamp(log.getCreatedAt())
        );
    }

    private static String mapLogType(AdminActivityType type) {
        return switch (type) {
            case FEE_PAID, FEE_INITIALIZED -> "FEE";
            case ATTENDANCE_UPDATED -> "ATTENDANCE";
            case CERTIFICATE_UPLOADED -> "CERTIFICATE";
            case STUDENT_PROMOTED -> "PROMOTION";
            case STUDENT_REGISTERED, STUDENT_PROFILE_CREATED, STUDENT_UPDATED, STUDENT_DEACTIVATED -> "PROFILE";
            default -> "OTHER";
        };
    }

    private static List<AdminActivityType> resolveActivityTypes(String logType) {
        if (logType == null || logType.isBlank() || "ALL".equalsIgnoreCase(logType)) {
            return null;
        }
        return switch (logType.toUpperCase()) {
            case "FEE" -> List.of(AdminActivityType.FEE_PAID, AdminActivityType.FEE_INITIALIZED);
            case "ATTENDANCE" -> List.of(AdminActivityType.ATTENDANCE_UPDATED);
            case "CERTIFICATE" -> List.of(AdminActivityType.CERTIFICATE_UPLOADED);
            case "PROMOTION" -> List.of(AdminActivityType.STUDENT_PROMOTED);
            case "PROFILE" -> List.of(
                    AdminActivityType.STUDENT_REGISTERED,
                    AdminActivityType.STUDENT_PROFILE_CREATED,
                    AdminActivityType.STUDENT_UPDATED,
                    AdminActivityType.STUDENT_DEACTIVATED);
            case "OTHER" -> List.of(AdminActivityType.OTHER);
            default -> null;
        };
    }

    private static String inferActorRole(String performedBy) {
        String lower = performedBy.toLowerCase(Locale.ROOT);
        if (lower.contains("teacher")) {
            return "Teacher";
        }
        if (lower.contains("parent")) {
            return "Parent";
        }
        return "Admin";
    }

    static String formatDisplayTimestamp(LocalDateTime at) {
        if (at == null) {
            return "";
        }
        LocalDate today = LocalDate.now();
        LocalDate date = at.toLocalDate();
        String time = at.format(TIME_FMT);
        if (date.equals(today)) {
            return "Today, " + time;
        }
        if (date.equals(today.minusDays(1))) {
            return "Yesterday, " + time;
        }
        return date.format(DATE_FMT) + ", " + time;
    }

    private void requireStudent(Long schoolId, Long studentId) {
        Student st = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!st.getSchool().getId().equals(schoolId)) {
            throw new ResourceNotFoundException("Student not found in school");
        }
    }
}
