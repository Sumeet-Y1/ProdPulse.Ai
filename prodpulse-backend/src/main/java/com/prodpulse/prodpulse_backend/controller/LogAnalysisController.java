package com.prodpulse.prodpulse_backend.controller;

import com.prodpulse.prodpulse_backend.model.dto.DiagnosisResponse;
import com.prodpulse.prodpulse_backend.model.dto.LogRequest;
import com.prodpulse.prodpulse_backend.service.LogAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for log analysis endpoints
 * Main API for ProdPulse.AI
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")  // Will be overridden by CorsConfig
public class LogAnalysisController {

    private static final Logger logger = LoggerFactory.getLogger(LogAnalysisController.class);

    @Autowired
    private LogAnalysisService logAnalysisService;

    /**
     * Main endpoint for analyzing production logs
     *
     * POST /api/analyze
     *
     * @param logRequest Request body containing error logs
     * @param request HttpServletRequest to extract IP address
     * @return DiagnosisResponse with AI-generated diagnosis
     */
    @PostMapping("/analyze")
    public ResponseEntity<DiagnosisResponse> analyzeLogs(
            @Valid @RequestBody LogRequest logRequest,
            HttpServletRequest request) {

        String ipAddress = getClientIpAddress(request);
        logger.info("Received log analysis request from IP: {}", ipAddress);

        DiagnosisResponse response = logAnalysisService.analyzeLogs(logRequest, ipAddress);

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     *
     * GET /api/health
     *
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "ProdPulse.AI Backend");
        health.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(health);
    }

    /**
     * Get rate limit status for current user
     *
     * GET /api/rate-limit-status
     *
     * @param request HttpServletRequest to extract IP address
     * @return Remaining requests count
     */
    @GetMapping("/rate-limit-status")
    public ResponseEntity<Map<String, Object>> getRateLimitStatus(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        int remaining = logAnalysisService.getRemainingRequests(ipAddress);

        Map<String, Object> status = new HashMap<>();
        status.put("remainingRequests", remaining);
        status.put("ipAddress", ipAddress);

        return ResponseEntity.ok(status);
    }

    /**
     * Root endpoint - API info
     *
     * GET /api/
     *
     * @return API information
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "ProdPulse.AI API");
        info.put("version", "1.0.0");
        info.put("description", "AI-powered production log analyzer");
        info.put("endpoints", Map.of(
                "POST /api/analyze", "Analyze production error logs",
                "GET /api/health", "Health check",
                "GET /api/rate-limit-status", "Check remaining requests"
        ));

        return ResponseEntity.ok(info);
    }

    /**
     * Extract client IP address from request
     * Handles proxy headers (X-Forwarded-For) for Railway deployment
     *
     * @param request HttpServletRequest
     * @return Client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        // Check proxy headers first (for Railway, Netlify, etc.)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        // Check other common proxy headers
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // Fallback to remote address
        return request.getRemoteAddr();
    }
}