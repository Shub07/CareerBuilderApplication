package com.org.careerbuilder.controller;

import com.org.careerbuilder.dto.request.VacationRequest;
import com.org.careerbuilder.dto.response.VacationResponse;
import com.org.careerbuilder.service.VacationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 🎫 Vacation Controller - Manages vacation operations
 * Admin endpoints for creating/updating vacations
 * Student endpoints for viewing vacations
 */
@Slf4j
@RestController
@RequestMapping("/api/vacations")
@RequiredArgsConstructor
public class VacationController {

    private final VacationService vacationService;

    // ===== STUDENT ENDPOINTS (Public) =====

    /**
     * Get all active vacations for a school (visible to all students)
     * GET /api/vacations/school/{schoolId}
     */
    @GetMapping("/school/{schoolId}")
    public ResponseEntity<Map<String, Object>> getSchoolVacations(@PathVariable Long schoolId) {
        log.info("Fetching vacations for school: {}", schoolId);

        try {
            List<VacationResponse> vacations = vacationService.getSchoolVacations(schoolId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vacations retrieved successfully");
            response.put("data", vacations);
            response.put("total_count", vacations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching vacations: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get upcoming vacations
     * GET /api/vacations/school/{schoolId}/upcoming
     */
    @GetMapping("/school/{schoolId}/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingVacations(@PathVariable Long schoolId) {
        log.info("Fetching upcoming vacations for school: {}", schoolId);

        try {
            List<VacationResponse> vacations = vacationService.getUpcomingVacations(schoolId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Upcoming vacations retrieved successfully");
            response.put("data", vacations);
            response.put("total_count", vacations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching upcoming vacations: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get ongoing vacations
     * GET /api/vacations/school/{schoolId}/ongoing
     */
    @GetMapping("/school/{schoolId}/ongoing")
    public ResponseEntity<Map<String, Object>> getOngoingVacations(@PathVariable Long schoolId) {
        log.info("Fetching ongoing vacations for school: {}", schoolId);

        try {
            List<VacationResponse> vacations = vacationService.getOngoingVacations(schoolId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Ongoing vacations retrieved successfully");
            response.put("data", vacations);
            response.put("total_count", vacations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching ongoing vacations: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get completed vacations
     * GET /api/vacations/school/{schoolId}/completed
     */
    @GetMapping("/school/{schoolId}/completed")
    public ResponseEntity<Map<String, Object>> getCompletedVacations(@PathVariable Long schoolId) {
        log.info("Fetching completed vacations for school: {}", schoolId);

        try {
            List<VacationResponse> vacations = vacationService.getCompletedVacations(schoolId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Completed vacations retrieved successfully");
            response.put("data", vacations);
            response.put("total_count", vacations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching completed vacations: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get single vacation details
     * GET /api/vacations/{vacationId}
     */
    @GetMapping("/{vacationId}")
    public ResponseEntity<Map<String, Object>> getVacation(@PathVariable Long vacationId) {
        log.info("Fetching vacation: {}", vacationId);

        try {
            VacationResponse vacation = vacationService.getVacationById(vacationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vacation retrieved successfully");
            response.put("data", vacation);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching vacation: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Check if school is on vacation
     * GET /api/vacations/school/{schoolId}/on-vacation
     */
    @GetMapping("/school/{schoolId}/on-vacation")
    public ResponseEntity<Map<String, Object>> isSchoolOnVacation(@PathVariable Long schoolId) {
        log.info("Checking if school is on vacation: {}", schoolId);

        try {
            boolean onVacation = vacationService.isSchoolOnVacation(schoolId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("school_id", schoolId);
            response.put("is_on_vacation", onVacation);
            response.put("message", onVacation ? "School is currently on vacation" : "School is not on vacation");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error checking vacation status: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ===== ADMIN ENDPOINTS =====

    /**
     * Create vacation (Admin only)
     * POST /api/vacations/admin/create
     */
    @PostMapping("/admin/create")
    public ResponseEntity<Map<String, Object>> createVacation(@Valid @RequestBody VacationRequest request) {
        log.info("Admin creating vacation: {}", request.getVacationName());

        try {
            VacationResponse vacation = vacationService.createVacation(request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vacation created successfully");
            response.put("data", vacation);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error creating vacation: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Update vacation (Admin only)
     * PUT /api/vacations/admin/{vacationId}
     */
    @PutMapping("/admin/{vacationId}")
    public ResponseEntity<Map<String, Object>> updateVacation(
            @PathVariable Long vacationId,
            @Valid @RequestBody VacationRequest request) {
        log.info("Admin updating vacation: {}", vacationId);

        try {
            VacationResponse vacation = vacationService.updateVacation(vacationId, request);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vacation updated successfully");
            response.put("data", vacation);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error updating vacation: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Delete vacation (Admin only)
     * DELETE /api/vacations/admin/{vacationId}
     */
    @DeleteMapping("/admin/{vacationId}")
    public ResponseEntity<Map<String, Object>> deleteVacation(@PathVariable Long vacationId) {
        log.info("Admin deleting vacation: {}", vacationId);

        try {
            vacationService.deleteVacation(vacationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vacation deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error deleting vacation: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Mark vacation notice as sent (Admin only)
     * POST /api/vacations/admin/{vacationId}/mark-notice-sent
     */
    @PostMapping("/admin/{vacationId}/mark-notice-sent")
    public ResponseEntity<Map<String, Object>> markNoticeAsSent(@PathVariable Long vacationId) {
        log.info("Admin marking vacation notice as sent: {}", vacationId);

        try {
            VacationResponse vacation = vacationService.markNoticeAsSent(vacationId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vacation notice marked as sent");
            response.put("data", vacation);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error marking notice as sent: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}

