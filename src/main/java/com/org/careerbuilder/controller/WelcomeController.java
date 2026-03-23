package com.org.careerbuilder.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    
    @GetMapping("/")
    public String welcome() {
        return "Welcome to Career Builder Backend API! Services are running on port 9090";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}

