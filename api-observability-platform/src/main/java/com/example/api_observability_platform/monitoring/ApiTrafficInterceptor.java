package com.example.api_observability_platform.monitoring;

import com.example.api_observability_platform.entity.ApiLog;
import com.example.api_observability_platform.entity.User;
import com.example.api_observability_platform.repository.UserRepository;
import com.example.api_observability_platform.service.MonitoringService;
import com.example.api_observability_platform.service.DiscordNotificationService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class ApiTrafficInterceptor implements HandlerInterceptor {

    private final MonitoringService monitoringService;
    private final UserRepository userRepository;
    private final DiscordNotificationService discordService;
    
    private final Bucket standardBucket; 
    private final Bucket jailBucket; 
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PENALTY_KEY_PREFIX = "penalty:user:";

    public ApiTrafficInterceptor(MonitoringService monitoringService, 
                                 UserRepository userRepository, 
                                 DiscordNotificationService discordService) {
        this.monitoringService = monitoringService;
        this.userRepository = userRepository;
        this.discordService = discordService;
        
        Bandwidth standardLimit = Bandwidth.classic(5000, Refill.greedy(5000, Duration.ofMinutes(1)));
        this.standardBucket = Bucket.builder().addLimit(standardLimit).build();

        Bandwidth restrictedLimit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));
        this.jailBucket = Bucket.builder().addLimit(restrictedLimit).build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        String traceId = "TR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        request.setAttribute("traceId", traceId);
        response.setHeader("X-Trace-ID", traceId);
        
        if (uri.startsWith("/api/analytics") || uri.startsWith("/api/monitoring") || 
            uri.startsWith("/auth/") || uri.contains("favicon")) {
            return true;
        }

        request.setAttribute("startTime", System.currentTimeMillis());

        String email = (String) request.getAttribute("authenticatedEmail");
        User user = null;
        if (email != null) {
            user = userRepository.findByEmail(email).orElse(null);
            request.setAttribute("authenticatedUser", user);
        }

        boolean isPenalized = false;
        if (user != null) {
            String strikeCount = monitoringService.getRedisValue(PENALTY_KEY_PREFIX + user.getId());
            if (strikeCount != null && Integer.parseInt(strikeCount) >= 3) {
                isPenalized = true;
            }
        }

        Bucket activeBucket = isPenalized ? jailBucket : standardBucket;

        if (!activeBucket.tryConsume(1)) {
            response.setStatus(429);
            response.getWriter().write("Security Alert: Your access is throttled due to multiple failed attempts.");
            return false; 
        }
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String uri = request.getRequestURI();
        int status = response.getStatus();

        if (uri.startsWith("/api/analytics") || uri.startsWith("/api/monitoring") || uri.startsWith("/auth/")) {
            return; 
        }

        User user = (User) request.getAttribute("authenticatedUser");
        Object startTimeAttr = request.getAttribute("startTime");
        String traceId = (String) request.getAttribute("traceId");
        long duration = (startTimeAttr != null) ? (System.currentTimeMillis() - (long) startTimeAttr) : 0;

        if (user != null || status >= 400) {
            ApiLog log = new ApiLog();
            log.setEndpoint(uri);
            log.setMethod(request.getMethod());
            log.setStatusCode(status);
            log.setResponseTime(duration);
            log.setTimestamp(LocalDateTime.now());
            
            String clientIp = request.getRemoteAddr();
            log.setClientIp(clientIp);
            log.setUser(user);
            log.setTraceId(traceId);

            String userAgent = request.getHeader("User-Agent");
            log.setBrowser(parseBrowser(userAgent)); // 🔥 Use updated parsing logic
            log.setCountry(getRealGeoLocation(clientIp));

            monitoringService.saveLog(log);

            if (user != null && (status == 401 || status == 404)) {
                String key = PENALTY_KEY_PREFIX + user.getId();
                monitoringService.incrementPenaltyStrike(key, 10);
                
                String strikes = monitoringService.getRedisValue(key);
                if (strikes != null && Integer.parseInt(strikes) == 3) {
                    discordService.sendAlert("🚨 AUTOMATIC BLOCK", 429, 0, 
                        "User " + user.getEmail() + " has reached 3 strikes and is now THROTTLED.");
                }
            }

            if (status >= 400 && status != 429) {
                String identity = (user != null) ? user.getEmail() : "Anonymous/Unauthorized Attempt";
                discordService.sendAlert(uri + " [Trace: " + traceId + "]", status, duration, identity);
            }
        }
    }

    // 🔥 UPDATED: Corrected Browser Parsing Logic
    private String parseBrowser(String ua) {
        if (ua == null) return "Unknown";
        
        // Order matters! Check for specific versions before general ones.
        if (ua.contains("Postman")) return "Postman";
        
        // Edge contains "Chrome" and "Safari" in its UA string, so check it FIRST
        if (ua.contains("Edg/") || ua.contains("Edge")) return "Edge";
        
        // Firefox is specific
        if (ua.contains("Firefox")) return "Firefox";
        
        // Chrome contains "Safari" in its string, so check Chrome BEFORE Safari
        if (ua.contains("Chrome")) return "Chrome";
        
        // If it made it here and has Safari, it's actually Safari
        if (ua.contains("Safari")) return "Safari";
        
        return "Other";
    }

    private String getRealGeoLocation(String ip) {
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
            return "India"; 
        }
        try {
            String url = "http://ip-api.com/json/" + ip;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && "success".equals(response.get("status"))) {
                return (String) response.get("country");
            }
        } catch (Exception e) {
            System.err.println("Geo-IP failed: " + e.getMessage());
        }
        return "Internal Network"; 
    }
}