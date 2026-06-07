package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.FacultyDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FacultyDocumentRepository extends JpaRepository<FacultyDocument, Long> {

    List<FacultyDocument> findByFaculty_IdOrderByUploadedAtDesc(Long facultyId);

    Optional<FacultyDocument> findByIdAndFaculty_Id(Long id, Long facultyId);
}
