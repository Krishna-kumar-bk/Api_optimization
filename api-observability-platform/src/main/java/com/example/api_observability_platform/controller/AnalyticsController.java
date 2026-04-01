package com.example.api_observability_platform.controller;

import com.example.api_observability_platform.dto.AnalyticsResponse;
import com.example.api_observability_platform.entity.User;
import com.example.api_observability_platform.repository.UserRepository;
import com.example.api_observability_platform.service.AnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository; // Added to find the user

    public AnalyticsController(AnalyticsService analyticsService, UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public AnalyticsResponse getStats() {
        // 1. Get the email from the Security Context (JWT)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // 2. Find the user in the database
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Pass the user to the service for personalized stats
        return analyticsService.getDashboardStats(currentUser);
    }
}