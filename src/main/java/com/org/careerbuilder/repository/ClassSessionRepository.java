package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassSession;
import com.org.careerbuilder.models.enums.SessionStatus;
import com.org.careerbuilder.models.enums.SessionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    List<ClassSession> findByClassNameAndSectionAndSessionDateOrderByStartTimeAsc(
            String className, String section, LocalDate date
    );

    long countByClassNameAndSectionAndSessionDateAndSessionTypeIn(
            String className, String section, LocalDate date, List<SessionType> types
    );

    long countByClassNameAndSectionAndSessionDateAndStatus(
            String className, String section, LocalDate date, SessionStatus status
    );
}
