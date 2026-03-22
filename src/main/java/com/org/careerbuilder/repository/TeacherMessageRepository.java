package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.TeacherMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherMessageRepository extends JpaRepository<TeacherMessage, Long> {
}