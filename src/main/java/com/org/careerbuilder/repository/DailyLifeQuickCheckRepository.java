package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.DailyLifeQuickCheck;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyLifeQuickCheckRepository extends JpaRepository<DailyLifeQuickCheck, Long> {

    List<DailyLifeQuickCheck> findByStudent_IdAndCheckDateOrderByIdAsc(Long studentId, LocalDate checkDate);
}