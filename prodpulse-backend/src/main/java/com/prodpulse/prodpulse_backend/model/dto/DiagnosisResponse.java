package com.prodpulse.prodpulse_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for diagnosis results
 * AI-generated diagnosis is returned via this response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisResponse {

    /**
     * Severity level: "critical", "warning", "info"
     */
    private String severity;

    /**
     * Brief title of the issue (e.g., "Database Connection Failure")
     */
    private String title;

    /**
     * Full diagnosis content from AI (HTML formatted)
     * Contains: What happened, How to fix, Prevention tips
     */
    private String content;

    /**
     * Timestamp of analysis
     */
    private String timestamp;

    /**
     * Analysis ID (for tracking)
     */
    private Long analysisId;

}