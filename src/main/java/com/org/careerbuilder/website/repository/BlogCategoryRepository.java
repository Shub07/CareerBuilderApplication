package com.org.careerbuilder.website.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.careerbuilder.website.models.BlogCategory;

public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {

    Optional<BlogCategory> findBySlug(String slug);

    List<BlogCategory> findAllByOrderByNameAsc();
}
