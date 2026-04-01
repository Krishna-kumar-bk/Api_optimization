package com.example.api_observability_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling; // 🔥 ADD THIS IMPORT

@SpringBootApplication
@EnableAsync
@EnableScheduling // 🔥 CRITICAL: This turns on the 5-second "Batch Saver" timer
@EnableCaching 
@EnableJpaRepositories(basePackages = "com.example.api_observability_platform.repository")
public class ApiObservabilityPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiObservabilityPlatformApplication.class, args);
    }
}