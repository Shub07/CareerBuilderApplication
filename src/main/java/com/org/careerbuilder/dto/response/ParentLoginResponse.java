package com.org.careerbuilder.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Parent Login Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentLoginResponse {
    private String token;
    private String parentId;
    private String email;
    private String message;
    private Long studentId;
    private String studentName;
    private Long timestamp;
}