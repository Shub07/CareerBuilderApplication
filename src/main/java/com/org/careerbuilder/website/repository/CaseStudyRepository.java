package com.org.careerbuilder.website.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.org.careerbuilder.website.models.CaseStudy;

public interface CaseStudyRepository extends JpaRepository<CaseStudy, Long> {

    Optional<CaseStudy> findBySlugAndPublishedTrue(String slug);

    @Query("""
            select c from CaseStudy c
            where c.published = true
              and (:q is null or (
                    lower(c.title) like lower(concat('%', :q, '%'))
                 or lower(coalesce(c.summary, '')) like lower(concat('%', :q, '%'))
              ))
            order by c.createdAt desc
            """)
    Page<CaseStudy> searchPublished(@Param("q") String q, Pageable pageable);
}
