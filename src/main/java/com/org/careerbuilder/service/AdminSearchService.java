package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.response.AdminOperationResponses;

public interface AdminSearchService {

    AdminOperationResponses.GlobalSearchResponse search(Long schoolId, String query, int limit);
}
