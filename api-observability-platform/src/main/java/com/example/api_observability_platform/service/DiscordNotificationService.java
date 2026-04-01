package com.example.api_observability_platform.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class DiscordNotificationService {

    // 🔥 We are hardcoding the URL here to bypass the application.properties error
    private final String webhookUrl = "https://discord.com/api/webhooks/1487721476705616003/zLnlcphDOVYhBga9HLQT_2y-qLftX3FeYq0zUMHclfZNGLMikSWVrQMPbrOY-tpy-BOt";

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendAlert(String endpoint, int status, long latency, String userEmail) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", "API Guard");
        body.put("avatar_url", "https://cdn-icons-png.flaticon.com/512/595/595067.png");

        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "⚠️ API Error Detected!");
        embed.put("color", status >= 500 ? 15158332 : 15844367); 
        
        String description = String.format(
            "**Endpoint:** `%s` \n**Status:** `%d` \n**Latency:** `%dms` \n**User:** %s",
            endpoint, status, latency, userEmail
        );
        embed.put("description", description);
        embed.put("timestamp", java.time.OffsetDateTime.now().toString());

        body.put("embeds", List.of(embed));

        try {
            restTemplate.postForEntity(webhookUrl, body, String.class);
            System.out.println("✅ Discord Alert Sent Successfully!");
        } catch (Exception e) {
            System.err.println("❌ Failed to send Discord alert: " + e.getMessage());
        }
    }
}