package com.prodpulse.prodpulse_backend.exception;

/**
 * Exception thrown when user exceeds rate limit
 * (e.g., more than 10 requests in 24 hours)
 */
public class RateLimitException extends RuntimeException {

    private final int maxRequests;
    private final int windowHours;

    public RateLimitException(int maxRequests, int windowHours) {
        super(String.format("Rate limit exceeded. Maximum %d requests allowed per %d hours.",
                maxRequests, windowHours));
        this.maxRequests = maxRequests;
        this.windowHours = windowHours;
    }

    public RateLimitException(String message) {
        super(message);
        this.maxRequests = 0;
        this.windowHours = 0;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public int getWindowHours() {
        return windowHours;
    }
}