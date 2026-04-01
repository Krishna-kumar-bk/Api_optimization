package com.example.api_observability_platform.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> checkHealth(@RequestParam(required = false) boolean simulateSlow) throws InterruptedException {
        
        // 🔥 SIMULATION LOGIC: If we want to test the "Slow" filter in React
        if (simulateSlow) {
            Thread.sleep(800); // Forces the request to take at least 800ms
        }

        return Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "capacity", simulateSlow ? "Throttled (Testing)" : "Ready for High Traffic",
            "latency_type", simulateSlow ? "SLOW_TEST" : "NORMAL",
            "features", List.of("Async-Logging", "Latency-Simulation", "Health-Monitor")
        );
    }
}