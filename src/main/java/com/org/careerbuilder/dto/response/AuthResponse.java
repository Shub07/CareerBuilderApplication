package com.org.careerbuilder.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        Long studentId,
        String role,
        String email,
        String mobile,
        String message
) {
}
