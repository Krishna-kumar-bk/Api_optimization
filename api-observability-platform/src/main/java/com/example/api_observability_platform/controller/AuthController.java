package com.example.api_observability_platform.controller;

import com.example.api_observability_platform.dto.AuthRequest;
import com.example.api_observability_platform.service.AuthService;
import com.example.api_observability_platform.entity.User;
import com.example.api_observability_platform.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            String token = authService.login(request);
            // Returns JSON object for React compatibility
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Credentials"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Not Authenticated");
        
        return userRepository.findByEmail(principal.getName())
            .map(user -> ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "id", user.getId()
            )))
            .orElse(ResponseEntity.notFound().build());
    }
}