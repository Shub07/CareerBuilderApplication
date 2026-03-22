package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.StudyMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyMaterialRepository extends JpaRepository<StudyMaterial, Long> {

    List<StudyMaterial> findBySubject_IdOrderByUploadedAtDesc(Long subjectId);

    List<StudyMaterial> findAllByOrderByUploadedAtDesc();
}
