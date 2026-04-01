package com.example.api_observability_platform.repository;

import com.example.api_observability_platform.entity.ApiLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional; // 🔥 Don't forget this import

public interface ApiLogRepository extends JpaRepository<ApiLog, Long> {
    
    // 1. Total Requests for specific user
    long countByUserId(Long userId);

    // 2. Average Latency for specific user
    @Query("SELECT AVG(l.responseTime) FROM ApiLog l WHERE l.user.id = :userId")
    Double getAverageResponseTimeByUser(@Param("userId") Long userId);

    // 3. Total Errors (400+) for specific user
    @Query("SELECT COUNT(l) FROM ApiLog l WHERE l.user.id = :userId AND l.statusCode >= 400")
    Long countErrorsByUser(@Param("userId") Long userId);
    
    // 4. Custom Analytics (Fast/Slow)
    long countByUserIdAndResponseTimeLessThan(Long userId, long responseTime);
    long countByUserIdAndResponseTimeGreaterThan(Long userId, long responseTime);
    
    // 5. Status Distribution for Pie Chart (Filtered by User)
    @Query("SELECT COUNT(l) FROM ApiLog l WHERE l.user.id = :userId AND l.statusCode >= 200 AND l.statusCode < 300")
    long count2xxByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM ApiLog l WHERE l.user.id = :userId AND l.statusCode >= 400 AND l.statusCode < 500")
    long count4xxByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM ApiLog l WHERE l.user.id = :userId AND l.statusCode >= 500")
    long count5xxByUser(@Param("userId") Long userId);
    
    // 6. Pagination Query
    Page<ApiLog> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    // 🔍 NEW: Trace ID Search Logic
    // This allows the dashboard to pull up the exact log for any specific TR-XXXX ID
    Optional<ApiLog> findByTraceId(String traceId);
}