package com.org.careerbuilder.website.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.org.careerbuilder.website.models.NewsItem;
import com.org.careerbuilder.website.models.NewsItem.NewsType;

public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {

    Optional<NewsItem> findBySlugAndPublishedTrue(String slug);

    Page<NewsItem> findByPublishedTrueAndTypeOrderByPublishedAtDesc(NewsType type, Pageable pageable);
}
