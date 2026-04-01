package com.example.api_observability_platform.dto;

public class ApiTestRequest {
    private String url;
    private String method; // GET, POST, PUT, DELETE
    private String body;   // JSON body for POST/PUT

    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}