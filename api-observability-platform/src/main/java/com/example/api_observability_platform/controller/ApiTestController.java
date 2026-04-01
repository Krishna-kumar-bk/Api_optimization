package com.example.api_observability_platform.controller;

import com.example.api_observability_platform.dto.ApiTestRequest;
import com.example.api_observability_platform.service.ApiTestService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-tool")
public class ApiTestController {

    private final ApiTestService apiTestService;

    public ApiTestController(ApiTestService apiTestService) {
        this.apiTestService = apiTestService;
    }

    @PostMapping("/run")
    public String runTest(@RequestBody ApiTestRequest request) {
        return apiTestService.executeTest(request.getUrl(), request.getMethod(), request.getBody());
    }
}