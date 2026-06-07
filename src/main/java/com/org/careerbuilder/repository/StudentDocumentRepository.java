package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {

    List<StudentDocument> findByStudent_IdOrderByUploadedAtDesc(Long studentId);
}
