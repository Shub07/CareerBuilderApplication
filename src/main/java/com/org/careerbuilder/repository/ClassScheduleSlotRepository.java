package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassScheduleSlotRepository extends JpaRepository<ClassScheduleSlot, Long> {

    List<ClassScheduleSlot> findBySchoolIdAndClassNameAndSectionAndDayOfWeekAndActiveTrueOrderByStartTime(
            Long schoolId, String className, String section, Integer dayOfWeek
    );

    List<ClassScheduleSlot> findBySchoolIdAndClassNameAndSectionAndSubject_IdAndDayOfWeekAndActiveTrueOrderByStartTime(
            Long schoolId, String className, String section, Long subjectId, Integer dayOfWeek
    );
}