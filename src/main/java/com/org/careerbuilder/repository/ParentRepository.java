package com.org.careerbuilder.repository;
import com.org.careerbuilder.models.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

    Optional<Parent> findByParentId(String parentId);

    boolean existsByParentId(String parentId);
}
