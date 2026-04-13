package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.VacationRequest;
import com.org.careerbuilder.dto.response.VacationResponse;
import com.org.careerbuilder.exceptions.VacationException;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.School;
import com.org.careerbuilder.models.Vacation;
import com.org.careerbuilder.repository.VacationRepository;
import com.org.careerbuilder.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 🎫 Vacation Service - Manages vacation operations
 * Admin creates vacations that are visible to all students
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VacationService {

    private final VacationRepository vacationRepository;
    private final SchoolRepository schoolRepository;

    // ===== ADMIN OPERATIONS =====

    /**
     * Create vacation by admin
     */
    @Transactional(rollbackFor = Exception.class)
    public VacationResponse createVacation(VacationRequest request) {
        log.info("Creating vacation: {} for school: {}", request.getVacationName(), request.getSchoolId());

        try {
            // Validate dates
            validateVacationDates(request.getStartDate(), request.getEndDate());

            // Get school
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("School not found: " + request.getSchoolId()));

            // Create vacation
            Vacation vacation = Vacation.builder()
                    .school(school)
                    .vacationName(request.getVacationName())
                    .vacationType(Vacation.VacationType.valueOf(request.getVacationType()))
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .description(request.getDescription())
                    .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                    .createdBy(request.getCreatedBy() != null ? request.getCreatedBy() : "ADMIN")
                    .build();

            vacation = vacationRepository.save(vacation);
            log.info("Vacation created successfully: {}", vacation.getId());

            return mapToResponse(vacation);

        } catch (VacationException | ResourceNotFoundException e) {
            log.error("Error creating vacation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error creating vacation: {}", e.getMessage(), e);
            throw new VacationException("Failed to create vacation: " + e.getMessage(), "VACATION_CREATE_ERROR", e);
        }
    }

    /**
     * Update vacation by admin
     */
    @Transactional(rollbackFor = Exception.class)
    public VacationResponse updateVacation(Long vacationId, VacationRequest request) {
        log.info("Updating vacation: {} for school: {}", vacationId, request.getSchoolId());

        try {
            // Validate dates
            validateVacationDates(request.getStartDate(), request.getEndDate());

            // Get vacation
            Vacation vacation = vacationRepository.findById(vacationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vacation not found: " + vacationId));

            // Verify school match
            if (!vacation.getSchool().getId().equals(request.getSchoolId())) {
                throw new VacationException("Vacation does not belong to this school", "VACATION_SCHOOL_MISMATCH");
            }

            // Update fields
            vacation.setVacationName(request.getVacationName());
            vacation.setVacationType(Vacation.VacationType.valueOf(request.getVacationType()));
            vacation.setStartDate(request.getStartDate());
            vacation.setEndDate(request.getEndDate());
            vacation.setDescription(request.getDescription());
            if (request.getIsActive() != null) {
                vacation.setIsActive(request.getIsActive());
            }
            vacation.setUpdatedBy("ADMIN");

            vacation = vacationRepository.save(vacation);
            log.info("Vacation updated successfully: {}", vacation.getId());

            return mapToResponse(vacation);

        } catch (VacationException | ResourceNotFoundException e) {
            log.error("Error updating vacation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error updating vacation: {}", e.getMessage(), e);
            throw new VacationException("Failed to update vacation: " + e.getMessage(), "VACATION_UPDATE_ERROR", e);
        }
    }

    /**
     * Delete vacation by admin
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteVacation(Long vacationId) {
        log.info("Deleting vacation: {}", vacationId);

        try {
            Vacation vacation = vacationRepository.findById(vacationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vacation not found: " + vacationId));

            vacationRepository.delete(vacation);
            log.info("Vacation deleted successfully: {}", vacationId);

        } catch (Exception e) {
            log.error("Error deleting vacation: {}", e.getMessage(), e);
            throw new VacationException("Failed to delete vacation: " + e.getMessage(), "VACATION_DELETE_ERROR", e);
        }
    }

    /**
     * Mark vacation notice as sent
     */
    @Transactional(rollbackFor = Exception.class)
    public VacationResponse markNoticeAsSent(Long vacationId) {
        log.info("Marking vacation notice as sent: {}", vacationId);

        try {
            Vacation vacation = vacationRepository.findById(vacationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vacation not found: " + vacationId));

            vacation.setNoticeSent(true);
            vacation.setNoticeSentDate(LocalDateTime.now());

            vacation = vacationRepository.save(vacation);
            log.info("Vacation notice marked as sent: {}", vacationId);

            return mapToResponse(vacation);

        } catch (Exception e) {
            log.error("Error marking notice as sent: {}", e.getMessage(), e);
            throw new VacationException("Failed to mark notice as sent: " + e.getMessage(), "NOTICE_ERROR", e);
        }
    }

    // ===== STUDENT/PUBLIC OPERATIONS =====

    /**
     * Get all vacations for school (visible to all students)
     */
    @Transactional(readOnly = true)
    public List<VacationResponse> getSchoolVacations(Long schoolId) {
        log.info("Fetching vacations for school: {}", schoolId);

        try {
            List<Vacation> vacations = vacationRepository.findActiveVacationsBySchoolId(schoolId);
            return vacations.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching vacations: {}", e.getMessage(), e);
            throw new VacationException("Failed to fetch vacations: " + e.getMessage(), "VACATION_FETCH_ERROR", e);
        }
    }

    /**
     * Get upcoming vacations
     */
    @Transactional(readOnly = true)
    public List<VacationResponse> getUpcomingVacations(Long schoolId) {
        log.info("Fetching upcoming vacations for school: {}", schoolId);

        try {
            List<Vacation> vacations = vacationRepository.findUpcomingVacations(schoolId);
            return vacations.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching upcoming vacations: {}", e.getMessage(), e);
            throw new VacationException("Failed to fetch upcoming vacations: " + e.getMessage(), "VACATION_FETCH_ERROR", e);
        }
    }

    /**
     * Get ongoing vacations
     */
    @Transactional(readOnly = true)
    public List<VacationResponse> getOngoingVacations(Long schoolId) {
        log.info("Fetching ongoing vacations for school: {}", schoolId);

        try {
            List<Vacation> vacations = vacationRepository.findOngoingVacations(schoolId);
            return vacations.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching ongoing vacations: {}", e.getMessage(), e);
            throw new VacationException("Failed to fetch ongoing vacations: " + e.getMessage(), "VACATION_FETCH_ERROR", e);
        }
    }

    /**
     * Get completed vacations
     */
    @Transactional(readOnly = true)
    public List<VacationResponse> getCompletedVacations(Long schoolId) {
        log.info("Fetching completed vacations for school: {}", schoolId);

        try {
            List<Vacation> vacations = vacationRepository.findCompletedVacations(schoolId);
            return vacations.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching completed vacations: {}", e.getMessage(), e);
            throw new VacationException("Failed to fetch completed vacations: " + e.getMessage(), "VACATION_FETCH_ERROR", e);
        }
    }

    /**
     * Get vacation by ID
     */
    @Transactional(readOnly = true)
    public VacationResponse getVacationById(Long vacationId) {
        log.info("Fetching vacation: {}", vacationId);

        try {
            Vacation vacation = vacationRepository.findById(vacationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vacation not found: " + vacationId));

            return mapToResponse(vacation);

        } catch (Exception e) {
            log.error("Error fetching vacation: {}", e.getMessage(), e);
            throw new VacationException("Failed to fetch vacation: " + e.getMessage(), "VACATION_FETCH_ERROR", e);
        }
    }

    /**
     * Check if school is on vacation
     */
    @Transactional(readOnly = true)
    public boolean isSchoolOnVacation(Long schoolId) {
        log.info("Checking if school is on vacation: {}", schoolId);

        try {
            return vacationRepository.isSchoolOnVacation(schoolId);

        } catch (Exception e) {
            log.error("Error checking vacation status: {}", e.getMessage(), e);
            return false;
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Validate vacation dates
     */
    private void validateVacationDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new VacationException("Start and end dates are required", "INVALID_DATES");
        }

        if (startDate.isAfter(endDate)) {
            throw new VacationException("Start date cannot be after end date", "INVALID_DATE_RANGE");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new VacationException("Start date cannot be in the past", "INVALID_START_DATE");
        }
    }

    /**
     * Map Vacation entity to VacationResponse DTO
     */
    private VacationResponse mapToResponse(Vacation vacation) {
        return VacationResponse.builder()
                .vacationId(vacation.getId())
                .schoolId(vacation.getSchool().getId())
                .vacationName(vacation.getVacationName())
                .vacationType(vacation.getVacationType().name())
                .vacationTypeLabel(vacation.getVacationType().getLabel())
                .startDate(vacation.getStartDate())
                .endDate(vacation.getEndDate())
                .durationDays(vacation.getDurationDays())
                .description(vacation.getDescription())
                .isActive(vacation.getIsActive())
                .isOngoing(vacation.isOngoing())
                .isUpcoming(vacation.isUpcoming())
                .isCompleted(vacation.isCompleted())
                .noticeSent(vacation.getNoticeSent())
                .noticeSentDate(vacation.getNoticeSentDate())
                .createdBy(vacation.getCreatedBy())
                .createdAt(vacation.getCreatedAt())
                .updatedAt(vacation.getUpdatedAt())
                .build();
    }
}

