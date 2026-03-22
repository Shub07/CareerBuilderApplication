package com.org.careerbuilder.website.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.website.models.NewsItem;
import com.org.careerbuilder.website.models.NewsItem.NewsType;
import com.org.careerbuilder.website.repository.NewsItemRepository;

@Service
public class NewsroomService {

    private final NewsItemRepository repository;

    public NewsroomService(NewsItemRepository repository) {
        this.repository = repository;
    }

    public Page<NewsItem> listPublishedByType(NewsType type, Pageable pageable) {
        return repository.findByPublishedTrueAndTypeOrderByPublishedAtDesc(type, pageable);
    }

    public NewsItem getPublishedBySlug(String slug) {
        return repository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("News item not found: " + slug));
    }
}
