package com.example.api_observability_platform.dto;

import java.util.List;
import java.util.Map;

public class AnalyticsResponse {
    private long totalRequests;
    private double avgLatency;
    private long totalErrors;
    private double successRate;
    private long fastRequests; 
    private long slowRequests;
    private long status2xx;
    private long status4xx;
    private long status5xx;
    
    private Map<String, Double> topEndpoints;
    private List<Map<String, Object>> timelineData;

    // 🔥 NEW: Analytics Fields for Geo and Browser Charts
    private Map<Object, Object> geoDistribution;     // { "India": 500, "USA": 200 }
    private Map<Object, Object> browserDistribution; // { "Chrome": 800, "Firefox": 100 }

    // 1. Default Constructor
    public AnalyticsResponse() {}

    // 2. Full Constructor
    public AnalyticsResponse(long totalRequests, double avgLatency, long totalErrors, 
                             double successRate, long fastRequests, long slowRequests,
                             long status2xx, long status4xx, long status5xx) {
        this.totalRequests = totalRequests;
        this.avgLatency = avgLatency;
        this.totalErrors = totalErrors;
        this.successRate = successRate;
        this.fastRequests = fastRequests;
        this.slowRequests = slowRequests;
        this.status2xx = status2xx;
        this.status4xx = status4xx;
        this.status5xx = status5xx;
    }

    // --- GETTERS ---
    public long getTotalRequests() { return totalRequests; }
    public double getAvgLatency() { return avgLatency; }
    public long getTotalErrors() { return totalErrors; }
    public double getSuccessRate() { return successRate; }
    public long getFastRequests() { return fastRequests; }
    public long getSlowRequests() { return slowRequests; }
    public long getStatus2xx() { return status2xx; }
    public long getStatus4xx() { return status4xx; }
    public long getStatus5xx() { return status5xx; }
    public Map<String, Double> getTopEndpoints() { return topEndpoints; }
    public List<Map<String, Object>> getTimelineData() { return timelineData; }
    
    // 🔥 NEW GETTERS
    public Map<Object, Object> getGeoDistribution() { return geoDistribution; }
    public Map<Object, Object> getBrowserDistribution() { return browserDistribution; }

    // --- SETTERS ---
    public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
    public void setAvgLatency(double avgLatency) { this.avgLatency = avgLatency; }
    public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    public void setFastRequests(long fastRequests) { this.fastRequests = fastRequests; }
    public void setSlowRequests(long slowRequests) { this.slowRequests = slowRequests; }
    public void setStatus2xx(long status2xx) { this.status2xx = status2xx; }
    public void setStatus4xx(long status4xx) { this.status4xx = status4xx; }
    public void setStatus5xx(long status5xx) { this.status5xx = status5xx; }
    public void setTopEndpoints(Map<String, Double> topEndpoints) { this.topEndpoints = topEndpoints; }
    public void setTimelineData(List<Map<String, Object>> timelineData) { this.timelineData = timelineData; }
    
    // 🔥 NEW SETTERS
    public void setGeoDistribution(Map<Object, Object> geoDistribution) { this.geoDistribution = geoDistribution; }
    public void setBrowserDistribution(Map<Object, Object> browserDistribution) { this.browserDistribution = browserDistribution; }
}