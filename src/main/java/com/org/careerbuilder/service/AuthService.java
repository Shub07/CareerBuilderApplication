package com.org.careerbuilder.service;

import com.org.careerbuilder.dto.request.LoginRequest;
import com.org.careerbuilder.dto.request.RegisterRequest;
import com.org.careerbuilder.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
