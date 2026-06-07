package com.org.careerbuilder.security;

import java.util.Set;

public class UserPrincipal {

    private final Long userId;
    private final Long studentId;
    private final Long schoolId;
    private final String email;
    private final Set<String> roles;

    public UserPrincipal(Long userId, Long studentId, Long schoolId, String email, Set<String> roles) {
        this.userId = userId;
        this.studentId = studentId;
        this.schoolId = schoolId;
        this.email = email;
        this.roles = roles;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
