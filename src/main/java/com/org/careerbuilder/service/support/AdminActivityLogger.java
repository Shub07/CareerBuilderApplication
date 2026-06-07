package com.org.careerbuilder.service.support;

import com.org.careerbuilder.models.AdminActivityLog;
import com.org.careerbuilder.models.School;
import com.org.careerbuilder.models.enums.AdminActivityType;
import com.org.careerbuilder.repository.AdminActivityLogRepository;
import com.org.careerbuilder.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminActivityLogger {

    private final AdminActivityLogRepository activityLogRepository;
    private final SchoolRepository schoolRepository;

    public void log(Long schoolId, AdminActivityType type, String title, String description,
                    String entityType, Long entityId, String performedBy) {
        School school = schoolRepository.findById(schoolId).orElse(null);
        if (school == null) {
            return;
        }
        activityLogRepository.save(AdminActivityLog.builder()
                .school(school)
                .activityType(type)
                .title(title)
                .description(description)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(performedBy != null ? performedBy : "School Admin")
                .build());
    }
}
