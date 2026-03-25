package com.org.careerbuilder.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "Duplicate or constraint violation",
                "detail", "Check unique fields (email/phone/code)"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "Unexpected error occurred"
        ));
    }
}
