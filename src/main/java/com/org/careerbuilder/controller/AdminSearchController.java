package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.response.AdminOperationResponses;
import com.org.careerbuilder.service.AdminSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/search")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:5174"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH,
                RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AdminSearchController {

    private final AdminSearchService adminSearchService;

    /**
     * Global search — students, teachers, classes.
     * GET /api/admin/search?schoolId=1&q=anita&limit=15
     */
    @GetMapping
    public ResponseEntity<AdminOperationResponses.GlobalSearchResponse> search(
            @RequestParam Long schoolId,
            @RequestParam String q,
            @RequestParam(defaultValue = "15") int limit) {
        return ResponseEntity.ok(adminSearchService.search(schoolId, q, limit));
    }
}
