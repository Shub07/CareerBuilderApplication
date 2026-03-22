package com.org.careerbuilder.website.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.website.models.CaseStudy;
import com.org.careerbuilder.website.repository.CaseStudyRepository;

@Service
public class CaseStudyService {

    private final CaseStudyRepository repository;

    public CaseStudyService(CaseStudyRepository repository) {
        this.repository = repository;
    }

    public Page<CaseStudy> listPublished(String search, Pageable pageable) {
        String q = (search == null || search.isBlank()) ? null : search;
        return repository.searchPublished(q, pageable);
    }

    public CaseStudy getPublishedBySlug(String slug) {
        return repository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Case study not found: " + slug));
    }
}
