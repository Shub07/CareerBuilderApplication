package com.org.careerbuilder.website.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.org.careerbuilder.website.models.NewsletterSubscriber;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<NewsletterSubscriber> findByEmailIgnoreCase(String email);
}
