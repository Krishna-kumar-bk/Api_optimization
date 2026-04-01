package com.example.api_observability_platform.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class ApiTestService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String executeTest(String url, String method, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, 
                HttpMethod.valueOf(method.toUpperCase()), 
                entity, 
                String.class
            );
            return response.getBody();
        } catch (Exception e) {
            return "Test Failed: " + e.getMessage();
        }
    }
}