package com.org.careerbuilder.website.service;

import org.springframework.stereotype.Service;

import com.org.careerbuilder.website.models.DemoRequest;
import com.org.careerbuilder.website.models.NewsletterSubscriber;
import com.org.careerbuilder.website.models.PartnerRequest;
import com.org.careerbuilder.website.repository.DemoRequestRepository;
import com.org.careerbuilder.website.repository.NewsletterSubscriberRepository;
import com.org.careerbuilder.website.repository.PartnerRequestRepository;

@Service
public class LeadService {

    private final DemoRequestRepository demoRequestRepository;
    private final NewsletterSubscriberRepository newsletterSubscriberRepository;
    private final PartnerRequestRepository partnerRequestRepository;

    public LeadService(DemoRequestRepository demoRequestRepository,
                       NewsletterSubscriberRepository newsletterSubscriberRepository,
                       PartnerRequestRepository partnerRequestRepository) {
        this.demoRequestRepository = demoRequestRepository;
        this.newsletterSubscriberRepository = newsletterSubscriberRepository;
        this.partnerRequestRepository = partnerRequestRepository;
    }

    public DemoRequest createDemoRequest(DemoRequest request) {
        return demoRequestRepository.save(request);
    }

    public PartnerRequest createPartnerRequest(PartnerRequest request) {
        return partnerRequestRepository.save(request);
    }

    public NewsletterSubscriber subscribe(String email) {
        NewsletterSubscriber existing = newsletterSubscriberRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existing != null) {
            if (!existing.isActive()) {
                existing.setActive(true);
                return newsletterSubscriberRepository.save(existing);
            }
            return existing;
        }
        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail(email);
        subscriber.setActive(true);
        return newsletterSubscriberRepository.save(subscriber);
    }
}
