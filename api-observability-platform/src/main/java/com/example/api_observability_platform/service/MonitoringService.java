package com.example.api_observability_platform.service;

import com.example.api_observability_platform.entity.ApiLog;
import com.example.api_observability_platform.entity.Alert;
import com.example.api_observability_platform.repository.ApiLogRepository;
import com.example.api_observability_platform.repository.AlertRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MonitoringService {

    private final ApiLogRepository apiLogRepository;
    private final AlertRepository alertRepository;
    private final StringRedisTemplate redisTemplate;

    private final ConcurrentLinkedQueue<ApiLog> logBuffer = new ConcurrentLinkedQueue<>();

    public MonitoringService(ApiLogRepository apiLogRepository, 
                             AlertRepository alertRepository, 
                             StringRedisTemplate redisTemplate) {
        this.apiLogRepository = apiLogRepository;
        this.alertRepository = alertRepository;
        this.redisTemplate = redisTemplate;
    }

    public void saveLog(ApiLog log) {
        Long userId = (log.getUser() != null) ? log.getUser().getId() : 0L;
        String userKey = "stats:user:" + userId;
        updateRedisStats(userKey, log);

        logBuffer.add(log);

        if (log.getResponseTime() > 2000 || log.getStatusCode() >= 500) {
            processAlerts(log);
        }

        if (logBuffer.size() >= 100) {
            flushLogsToDatabase();
        }
    }

    private void updateRedisStats(String key, ApiLog log) {
        redisTemplate.opsForHash().increment(key, "totalRequests", 1);
        String endpointKey = key + ":endpoints";
        redisTemplate.opsForZSet().incrementScore(endpointKey, log.getEndpoint(), 1);

        if (log.getCountry() != null) {
            redisTemplate.opsForHash().increment(key + ":geo", log.getCountry(), 1);
        }
        if (log.getBrowser() != null) {
            redisTemplate.opsForHash().increment(key + ":browsers", log.getBrowser(), 1);
        }

        String timeMinute = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm"));
        String timelineKey = key + ":timeline:" + timeMinute;
        String statusField = log.getStatusCode() >= 400 ? "fail" : "success";
        
        redisTemplate.opsForHash().increment(timelineKey, statusField, 1);
        redisTemplate.expire(timelineKey, Duration.ofHours(24));

        if (log.getStatusCode() >= 200 && log.getStatusCode() < 300) 
            redisTemplate.opsForHash().increment(key, "status2xx", 1);
        else if (log.getStatusCode() >= 400 && log.getStatusCode() < 500) 
            redisTemplate.opsForHash().increment(key, "status4xx", 1);
        else if (log.getStatusCode() >= 500) 
            redisTemplate.opsForHash().increment(key, "status5xx", 1);

        if (log.getStatusCode() >= 400) 
            redisTemplate.opsForHash().increment(key, "totalErrors", 1);

        if (log.getResponseTime() < 200) 
            redisTemplate.opsForHash().increment(key, "fastRequests", 1);
        else if (log.getResponseTime() > 1000) 
            redisTemplate.opsForHash().increment(key, "slowRequests", 1);
    }

    @Scheduled(fixedRate = 5000)
    public void flushLogsToDatabase() {
        if (logBuffer.isEmpty()) return;

        List<ApiLog> batch = new ArrayList<>();
        ApiLog currentLog;
        
        while ((currentLog = logBuffer.poll()) != null) {
            batch.add(currentLog);
        }

        if (!batch.isEmpty()) {
            apiLogRepository.saveAll(batch); 
        }
    }

    private void processAlerts(ApiLog log) {
        String type = log.getStatusCode() >= 500 ? "SERVER_ERROR" : "SLOW_API";
        Alert alert = new Alert();
        alert.setEndpoint(log.getEndpoint());
        alert.setAlertType(type);
        alert.setMessage(type.equals("SERVER_ERROR") ? "Critical Error" : "Latency Spike");
        alert.setCreatedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    public Page<ApiLog> getRecentLogs(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return apiLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    // 🔥 NEW: REDIS PENALTY HELPERS FOR DYNAMIC THROTTLING

    /**
     * Increments the "Strike" count in Redis.
     * If strikes hit 3, the user is penalized for 10 minutes.
     */
    public void incrementPenaltyStrike(String key, int penaltyMinutes) {
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofMinutes(penaltyMinutes));
    }

    /**
     * Reads the current strike count from Redis.
     */
    public String getRedisValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}