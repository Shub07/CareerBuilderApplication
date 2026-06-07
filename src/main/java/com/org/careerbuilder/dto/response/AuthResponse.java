package com.org.careerbuilder.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        Long studentId,
        Long facultyId,
        Long schoolId,
        String role,
        String email,
        String mobile,
        String message
) {
}
