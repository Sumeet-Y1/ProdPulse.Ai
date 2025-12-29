package com.prodpulse.prodpulse_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response for API errors
 * Used when validation fails, rate limit exceeded, etc.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    /**
     * HTTP status code (e.g., 400, 429, 500)
     */
    private int status;

    /**
     * Error message for the user
     */
    private String message;

    /**
     * Detailed error description (optional)
     */
    private String details;

    /**
     * Timestamp when error occurred
     */
    private LocalDateTime timestamp;

    /**
     * API endpoint where error occurred
     */
    private String path;

}