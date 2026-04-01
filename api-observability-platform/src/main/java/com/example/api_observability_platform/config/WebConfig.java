package com.example.api_observability_platform.config;

import com.example.api_observability_platform.monitoring.ApiTrafficInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiTrafficInterceptor trafficInterceptor;

    public WebConfig(ApiTrafficInterceptor trafficInterceptor) {
        this.trafficInterceptor = trafficInterceptor;
    }

    // This part handles the Database Logs (Keep this)
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trafficInterceptor)
                .addPathPatterns("/**")
                // DO NOT log these paths to prevent loops and empty user logs
                .excludePathPatterns("/api/analytics/**", "/auth/**", "/static/**", "/favicon.ico");
    }

    // ADD THIS PART: This handles the Dashboard Connection (CORS)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") 
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}