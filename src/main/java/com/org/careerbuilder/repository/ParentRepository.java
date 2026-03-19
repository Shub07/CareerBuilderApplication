package com.org.careerbuilder.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.org.careerbuilder.models.Parents;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parents, Long> {

    Optional<Parents> findByParentId(String parentId);

    boolean existsByParentId(String parentId);
}
