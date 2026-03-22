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

import com.org.careerbuilder.website.models.NewsItem;
import com.org.careerbuilder.website.models.NewsItem.NewsType;
import com.org.careerbuilder.website.service.NewsroomService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/newsroom")
public class NewsroomController {

    private final NewsroomService newsroomService;

    public NewsroomController(NewsroomService newsroomService) {
        this.newsroomService = newsroomService;
    }

    @GetMapping("/press-releases")
    public Page<NewsItem> listPressReleases(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        log.info("API: List press releases page={} size={}", page, size);
        return newsroomService.listPublishedByType(NewsType.PRESS_RELEASE, pageable);
    }

    @GetMapping("/announcements")
    public Page<NewsItem> listAnnouncements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        log.info("API: List announcements page={} size={}", page, size);
        return newsroomService.listPublishedByType(NewsType.ANNOUNCEMENT, pageable);
    }

    @GetMapping("/{slug}")
    public NewsItem getNewsItem(@PathVariable String slug) {
        log.info("API: Get news item slug={}", slug);
        return newsroomService.getPublishedBySlug(slug);
    }
}
