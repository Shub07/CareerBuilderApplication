package com.org.careerbuilder.website.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.website.models.BlogCategory;
import com.org.careerbuilder.website.models.BlogPost;
import com.org.careerbuilder.website.repository.BlogCategoryRepository;
import com.org.careerbuilder.website.repository.BlogPostRepository;

@Service
public class BlogService {

    private final BlogPostRepository blogPostRepository;
    private final BlogCategoryRepository blogCategoryRepository;

    public BlogService(BlogPostRepository blogPostRepository, BlogCategoryRepository blogCategoryRepository) {
        this.blogPostRepository = blogPostRepository;
        this.blogCategoryRepository = blogCategoryRepository;
    }

    public Page<BlogPost> listPublishedPosts(String categorySlug, String search, Pageable pageable) {
        String category = (categorySlug == null || categorySlug.isBlank()) ? null : categorySlug;
        String q = (search == null || search.isBlank()) ? null : search;
        return blogPostRepository.searchPublished(category, q, pageable);
    }

    public BlogPost getPublishedPostBySlug(String slug) {
        return blogPostRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + slug));
    }

    public List<BlogCategory> listCategories() {
        return blogCategoryRepository.findAllByOrderByNameAsc();
    }
}
