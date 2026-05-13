package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ClassAttendanceEditAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassAttendanceEditAuditRepository extends JpaRepository<ClassAttendanceEditAudit, Long> {

    List<ClassAttendanceEditAudit> findTop20BySession_IdOrderByEditedAtDesc(Long sessionId);
}
