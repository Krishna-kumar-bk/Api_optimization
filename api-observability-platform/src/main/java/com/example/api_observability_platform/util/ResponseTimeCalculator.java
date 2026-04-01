package com.example.api_observability_platform.util;

public class ResponseTimeCalculator {

    // Helper to format ms into seconds if the response is very slow
    public static String formatTime(long ms) {
        if (ms < 1000) return ms + "ms";
        return (ms / 1000.0) + "s";
    }

    // Threshold logic: Anything above 2000ms is considered "Slow"
    public static boolean isSlow(long ms) {
        return ms > 2000;
    }
}