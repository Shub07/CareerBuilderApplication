package com.org.careerbuilder.website.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.org.careerbuilder.website.models.CaseStudy;
import com.org.careerbuilder.website.service.CaseStudyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/case-studies")
public class CaseStudyController {

    private final CaseStudyService service;

    public CaseStudyController(CaseStudyService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CaseStudy> list(
            @RequestParam(required = false, name = "search") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        log.info("API: List case studies q={} page={} size={}", q, page, size);
        return service.listPublished(q, pageable);
    }

    @GetMapping("/{slug}")
    public CaseStudy get(@PathVariable String slug) {
        log.info("API: Get case study slug={}", slug);
        return service.getPublishedBySlug(slug);
    }
}
