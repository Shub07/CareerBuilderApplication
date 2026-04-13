package com.org.careerbuilder.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email(message = "Invalid email format")
        String email,
        @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile must be 10 to 15 digits")
        String mobile,
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be 6 to 100 characters")
        String password,
        @NotBlank(message = "Role is required")
        String role,
                Long studentId,
                String firstName,
                String lastName,
                @Min(value = 3, message = "Age must be >= 3")
                @Max(value = 100, message = "Age must be <= 100")
                Integer age,
                String className,
                String section,
                @Min(value = 1, message = "Roll number must be >= 1")
                Integer rollNo,
                String parentName,
                @Pattern(regexp = "^[0-9]{10,15}$", message = "Student phone must be 10 to 15 digits")
                String studentPhone,
                @Email(message = "Invalid student email format")
                String studentEmail,
                String address,
                Long schoolId
) {

        public boolean hasStudentProfilePayload() {
                return firstName != null || lastName != null || age != null || className != null
                                || section != null || rollNo != null || parentName != null || studentPhone != null
                                || studentEmail != null || address != null || schoolId != null;
        }
}
