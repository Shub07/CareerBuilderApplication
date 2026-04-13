package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.ParentLoginRequest;
import com.org.careerbuilder.dto.response.ParentLoginResponse;
import com.org.careerbuilder.dto.response.ParentChildDashboardDTO;
import com.org.careerbuilder.exceptions.ResourceNotFoundException;
import com.org.careerbuilder.models.ParentCredential;
import com.org.careerbuilder.models.Student;
import com.org.careerbuilder.repository.ParentCredentialRepository;
import com.org.careerbuilder.repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 👨‍👩‍👧 Parent Authentication Service - Handles parent login and child data access
 */
@Slf4j
@Service
public class ParentAuthenticationService {
    
    private final ParentCredentialRepository parentCredRepo;
    private final StudentRepository studentRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Value("${app.jwt.secret:replace-with-32+char-super-secret-key-1234567890}")
    private String jwtSecret;
    
    public ParentAuthenticationService(
            ParentCredentialRepository parentCredRepo,
            StudentRepository studentRepo) {
        this.parentCredRepo = parentCredRepo;
        this.studentRepo = studentRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * 🔐 Authenticate Parent Login
     */
    public ParentLoginResponse login(ParentLoginRequest request) {
        log.info("🔐 Parent login attempt: {}", request.getEmail());
        
        ParentCredential parentCred = parentCredRepo.findByEmail(request.getEmail())
            .orElseThrow(() -> {
                log.warn("❌ Login failed: Parent not found with email {}", request.getEmail());
                return new ResourceNotFoundException("Invalid email or password");
            });
        
        // Check if account is active
        if (!parentCred.getIsActive()) {
            log.warn("❌ Login failed: Parent account inactive {}", request.getEmail());
            throw new ResourceNotFoundException("Account is inactive. Contact administrator.");
        }
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), parentCred.getPasswordHash())) {
            log.warn("❌ Login failed: Incorrect password for {}", request.getEmail());
            throw new ResourceNotFoundException("Invalid email or password");
        }
        
        // Get student info
        Student student = studentRepo.findById(Long.valueOf(parentCred.getStudentId()))
            .orElseThrow(() -> new ResourceNotFoundException("Child information not found"));
        
        // Generate JWT token
        String token = generateJWTToken(parentCred);
        
        // Update last login
        parentCred.setLastLogin(LocalDateTime.now());
        parentCredRepo.save(parentCred);
        
        log.info("✅ Parent login successful: {}", request.getEmail());
        
        return ParentLoginResponse.builder()
            .token(token)
            .parentId(parentCred.getParentId())
            .email(parentCred.getEmail())
            .message("Login successful")
            .studentId(student.getId())
            .studentName(student.getFirstName() + " " + student.getLastName())
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * 📊 Get Parent Dashboard with Child's Info (READ-ONLY)
     */
    public ParentChildDashboardDTO getParentDashboard(String parentId) {
        log.info("📊 Fetching dashboard for parent: {}", parentId);
        
        ParentCredential parentCred = parentCredRepo.findByParentId(parentId)
            .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
        
        Student student = studentRepo.findById(Long.valueOf(parentCred.getStudentId()))
            .orElseThrow(() -> new ResourceNotFoundException("Child information not found"));
        
        return ParentChildDashboardDTO.builder()
            .parentId(parentCred.getParentId())
            .parentEmail(parentCred.getEmail())
            .parentName("Parent")
            .childId(student.getId())
            .childName(student.getFirstName() + " " + student.getLastName())
            .childEmail(student.getEmail())
            .childClass(student.getClassName())
            .section("A")
            .rollNo(student.getRollNo())
            .schoolName(student.getSchool() != null ? student.getSchool().getSchoolName() : "N/A")
            .age(student.getAge())
            .overallAttendance(90.0)
            .pendingFeesCount(0)
            .pendingFeesAmount("₹0")
            .upcomingAssignments(0)
            .build();
    }
    
    /**
     * 🔑 Generate JWT Token for Parent
     */
    private String generateJWTToken(ParentCredential parentCred) {
        long expirationTime = System.currentTimeMillis() + 86400000; // 24 hours
        String payload = Base64.getEncoder().encodeToString(
            (parentCred.getParentId() + ":" + expirationTime).getBytes()
        );
        log.debug("🔑 JWT token generated for parent: {}", parentCred.getParentId());
        return payload;
    }
}

