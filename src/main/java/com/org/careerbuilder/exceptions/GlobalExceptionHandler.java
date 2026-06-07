package com.org.careerbuilder.exceptions;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(Map.of(
                "message", "Validation failed",
                "errors", errors
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(DataIntegrityViolationException ex) {
        String rawMessage = ex.getMostSpecificCause() != null
            ? ex.getMostSpecificCause().getMessage()
            : ex.getMessage();
        String normalizedMessage = rawMessage == null ? "" : rawMessage.toLowerCase(Locale.ROOT);

        if (normalizedMessage.contains("uk_myclass_student_subject") ||
            (normalizedMessage.contains("my_classes") &&
                normalizedMessage.contains("student_id") &&
                normalizedMessage.contains("subject_id"))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "This subject is already added for this student.",
                "code", "MYCLASS_DUPLICATE_SUBJECT",
                "detail", "Choose a different subject for this student or update the existing class entry."
            ));
        }

        if (normalizedMessage.contains("app_users_role_check")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "This account role is not allowed by the database.",
                "detail", "Run migration V1048__app_users_role_check_expand.sql to allow SCHOOL_ADMIN and PARENT registration."
            ));
        }

        if (normalizedMessage.contains("uk_app_users_email")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Email is already registered.",
                "detail", "Use Login instead, or register with a different email."
            ));
        }

        if (normalizedMessage.contains("uk_app_users_mobile")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Mobile number is already registered.",
                "detail", "Use Login instead, or register with a different mobile number."
            ));
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Duplicate or constraint violation",
                "detail", "Check unique fields (email/phone/code)"
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "message", ex.getMessage() != null ? ex.getMessage() : "Access denied"
        ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "message", "Authentication required"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "Unexpected error occurred",
                "detail", ex.getMessage() != null ? ex.getMessage() : "No details available"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", ex.getMessage()
        ));
    }
}
