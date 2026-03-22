package com.org.careerbuilder.service;

import com.org.careerbuilder.models.ClassSession;
import com.org.careerbuilder.models.enums.SessionStatus;
import com.org.careerbuilder.models.enums.SessionType;
import com.org.careerbuilder.repository.ClassSessionRepository;
import com.org.careerbuilder.service.ClassScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassScheduleServiceImpl implements ClassScheduleService {

    private final ClassSessionRepository classSessionRepository;

    @Override
    public List<ClassSession> getTodaySessions(String className, String section, LocalDate date) {
        return classSessionRepository.findByClassNameAndSectionAndSessionDateOrderByStartTimeAsc(className, section, date);
    }

    @Override
    public int countTodayClasses(String className, String section, LocalDate date) {
        long count = classSessionRepository.countByClassNameAndSectionAndSessionDateAndSessionTypeIn(
                className, section, date, List.of(SessionType.REGULAR, SessionType.SPECIAL)
        );
        return (int) count;
    }

    @Override
    public int countTodayCompletedClasses(String className, String section, LocalDate date) {
        long count = classSessionRepository.countByClassNameAndSectionAndSessionDateAndStatus(
                className, section, date, SessionStatus.COMPLETED
        );
        return (int) count;
    }
}
