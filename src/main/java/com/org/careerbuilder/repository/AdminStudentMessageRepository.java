package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.AdminStudentMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminStudentMessageRepository extends JpaRepository<AdminStudentMessage, Long> {

    List<AdminStudentMessage> findTop20ByStudent_IdOrderByCreatedAtDesc(Long studentId);
}
