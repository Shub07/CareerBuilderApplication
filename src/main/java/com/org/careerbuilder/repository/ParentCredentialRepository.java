package com.org.careerbuilder.repository;

import com.org.careerbuilder.models.ParentCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 👨‍👩‍👧 Parent Credential Repository
 */
@Repository
public interface ParentCredentialRepository extends JpaRepository<ParentCredential, Long> {
    Optional<ParentCredential> findByEmail(String email);
    Optional<ParentCredential> findByParentId(String parentId);
    Optional<ParentCredential> findByStudentId(String studentId);
    boolean existsByEmail(String email);
}

