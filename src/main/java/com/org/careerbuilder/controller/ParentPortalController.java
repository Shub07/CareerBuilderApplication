package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.ParentLoginRequest;
import com.org.careerbuilder.dto.response.ParentLoginResponse;
import com.org.careerbuilder.dto.response.ParentChildDashboardDTO;
import com.org.careerbuilder.service.ParentAuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 👨‍👩‍👧 Parent Portal Controller - Handles parent authentication and child data access
 */
@Slf4j
@RestController
@RequestMapping("/api/parent-portal")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ParentPortalController {
    
    private final ParentAuthenticationService authService;
    
    public ParentPortalController(ParentAuthenticationService authService) {
        this.authService = authService;
    }
    
    /**
     * 🔐 Parent Login Endpoint
     * POST /api/parent-portal/login
     * Request: {"email": "parent@example.com", "password": "password123"}
     * Returns: ParentLoginResponse with token and child info
     */
    @PostMapping("/login")
    public ResponseEntity<ParentLoginResponse> parentLogin(@Valid @RequestBody ParentLoginRequest request) {
        log.info("🔐 Parent portal login request: {}", request.getEmail());
        ParentLoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 📊 Get Parent Dashboard - Child's Data (READ-ONLY)
     * GET /api/parent-portal/dashboard/{parentId}
     * Returns: ParentChildDashboardDTO with child's information
     */
    @GetMapping("/dashboard/{parentId}")
    public ResponseEntity<ParentChildDashboardDTO> getParentDashboard(@PathVariable String parentId) {
        log.info("📊 Fetching dashboard for parent: {}", parentId);
        ParentChildDashboardDTO dashboard = authService.getParentDashboard(parentId);
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * ✅ Verify Parent Credentials
     * GET /api/parent-portal/verify/{parentId}
     * Returns: Simple verification status
     */
    @GetMapping("/verify/{parentId}")
    public ResponseEntity<?> verifyParent(@PathVariable String parentId) {
        log.info("✅ Verifying parent: {}", parentId);
        try {
            authService.getParentDashboard(parentId);
            return ResponseEntity.ok("{\"verified\": true}");
        } catch (Exception e) {
            return ResponseEntity.ok("{\"verified\": false}");
        }
    }
}

