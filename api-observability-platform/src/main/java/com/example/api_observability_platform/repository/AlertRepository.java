package com.example.api_observability_platform.repository;

import com.example.api_observability_platform.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    // Fetch alerts for a specific troubled endpoint
    List<Alert> findByEndpointOrderByCreatedAtDesc(String endpoint);
}