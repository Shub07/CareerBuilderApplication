package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherDailyActivityReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TeacherDailyActivityReviewRepository extends JpaRepository<TeacherDailyActivityReview, Long> {
    Optional<TeacherDailyActivityReview> findByFaculty_IdAndStudent_IdAndActivityDate(Long facultyId, Long studentId, LocalDate activityDate);

    List<TeacherDailyActivityReview> findByFaculty_IdAndActivityDateAndStudent_IdIn(Long facultyId, LocalDate activityDate, Collection<Long> studentIds);
}
