package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Long>, JpaSpecificationExecutor<LearningMaterial> {

    List<LearningMaterial> findBySchoolIdAndClassNameAndSectionAndSubject_IdAndVisibleToStudentsIsTrueOrderByCreatedAtDesc(
            Long schoolId, String className, String section, Long subjectId);
}
