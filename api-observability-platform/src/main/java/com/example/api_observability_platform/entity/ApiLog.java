package com.example.api_observability_platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_logs")
public class ApiLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String endpoint;
    private String method;
    private int statusCode;
    private long responseTime; 
    private LocalDateTime timestamp;
    private String clientIp;

    // 🔥 NEW: Analytics Fields
    private String country;   
    
    @Column(columnDefinition = "TEXT") 
    private String browser;   

    // 🔍 NEW: Distributed Tracing Field
    private String traceId; // e.g., "TR-A1B2C3D4"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Standard Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public long getResponseTime() { return responseTime; }
    public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    // 🔍 Trace ID Getter and Setter
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}