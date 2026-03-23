package com.org.careerbuilder.website.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.org.careerbuilder.website.models.BlogPost;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    Optional<BlogPost> findBySlugAndPublishedTrue(String slug);

    @Query("""
            select p from BlogPost p
            where p.published = true
              and (:category is null or p.categorySlug = :category)
              and (:q is null or (
                    lower(p.title) like lower(concat('%', :q, '%'))
                 or lower(coalesce(p.summary, '')) like lower(concat('%', :q, '%'))
                 or lower(cast(p.content as string)) like lower(concat('%', :q, '%'))
              ))
            order by p.createdAt desc
            """)
    Page<BlogPost> searchPublished(@Param("category") String category,
                                  @Param("q") String q,
                                  Pageable pageable);
}
