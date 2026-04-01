package com.example.api_observability_platform.controller;

import com.example.api_observability_platform.entity.ApiLog;
import com.example.api_observability_platform.entity.User;
import com.example.api_observability_platform.repository.ApiLogRepository;
import com.example.api_observability_platform.repository.UserRepository;
import com.example.api_observability_platform.service.MonitoringService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;
    private final UserRepository userRepository;
    private final ApiLogRepository apiLogRepository; // 🔥 Added repository

    public MonitoringController(MonitoringService monitoringService, 
                                UserRepository userRepository,
                                ApiLogRepository apiLogRepository) {
        this.monitoringService = monitoringService;
        this.userRepository = userRepository;
        this.apiLogRepository = apiLogRepository;
    }

    @GetMapping("/logs")
    public Page<ApiLog> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return monitoringService.getRecentLogs(currentUser.getId(), page, size);
    }

    /**
     * 🔍 NEW: Trace ID Deep Search
     * Allows developers to find a specific request by its unique Trace ID.
     */
    @GetMapping("/trace/{traceId}")
    public ResponseEntity<ApiLog> getLogByTrace(@PathVariable String traceId) {
        return apiLogRepository.findByTraceId(traceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}