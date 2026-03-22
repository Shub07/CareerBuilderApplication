package com.org.careerbuilder.security;

import java.util.Set;

public class UserPrincipal {
    private final Long studentId;
    private final String email;
    private final Set<String> roles;

    public UserPrincipal(Long studentId, String email, Set<String> roles) {
        this.studentId = studentId;
        this.email = email;
        this.roles = roles;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getRoles() {
        return roles;
    }
}