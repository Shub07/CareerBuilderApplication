package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.CareerStudentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerStudentAnswerRepository extends JpaRepository<CareerStudentAnswer, Long> {

    List<CareerStudentAnswer> findByProgress_Id(Long progressId);

    Optional<CareerStudentAnswer> findByProgress_IdAndQuestion_Id(Long progressId, Long questionId);

    void deleteByProgress_Id(Long progressId);
}
