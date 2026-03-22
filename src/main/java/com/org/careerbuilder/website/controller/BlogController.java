package com.org.careerbuilder.website.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.org.careerbuilder.website.models.BlogCategory;
import com.org.careerbuilder.website.models.BlogPost;
import com.org.careerbuilder.website.service.BlogService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/blog")
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping("/posts")
    public Page<BlogPost> listPosts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "search") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        log.info("API: List blog posts category={} q={} page={} size={}", category, q, page, size);
        return blogService.listPublishedPosts(category, q, pageable);
    }

    @GetMapping("/posts/{slug}")
    public BlogPost getPost(@PathVariable String slug) {
        log.info("API: Get blog post slug={}", slug);
        return blogService.getPublishedPostBySlug(slug);
    }

    @GetMapping("/categories")
    public List<BlogCategory> listCategories() {
        log.info("API: List blog categories");
        return blogService.listCategories();
    }
}
