package com.org.careerbuilder.service;

import com.org.careerbuilder.models.ClassSession;

import java.time.LocalDate;
import java.util.List;

public interface ClassScheduleService {
    List<ClassSession> getTodaySessions(String className, String section, LocalDate date);
    int countTodayClasses(String className, String section, LocalDate date);
    int countTodayCompletedClasses(String className, String section, LocalDate date);
}
