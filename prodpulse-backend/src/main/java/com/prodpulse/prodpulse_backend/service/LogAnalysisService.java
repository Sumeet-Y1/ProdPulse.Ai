package com.prodpulse.prodpulse_backend.service;

import com.prodpulse.prodpulse_backend.exception.InvalidLogException;
import com.prodpulse.prodpulse_backend.exception.RateLimitException;
import com.prodpulse.prodpulse_backend.model.dto.DiagnosisResponse;
import com.prodpulse.prodpulse_backend.model.dto.LogRequest;
import com.prodpulse.prodpulse_backend.model.entity.AnalysisHistory;
import com.prodpulse.prodpulse_backend.repository.AnalysisHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Main service for log analysis
 * Handles rate limiting, validation, and orchestrates AI analysis
 */
@Service
public class LogAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(LogAnalysisService.class);

    @Autowired
    private AIService aiService;

    @Autowired
    private AnalysisHistoryRepository analysisHistoryRepository;

    // Rate limit configuration from application.properties
    @Value("${app.rate-limit.max-requests:10}")
    private int maxRequests;

    @Value("${app.rate-limit.window-hours:24}")
    private int windowHours;

    /**
     * Analyze production error logs
     *
     * @param logRequest Request containing error logs
     * @param ipAddress User's IP address (for rate limiting)
     * @return Diagnosis response from AI
     * @throws RateLimitException if user exceeded rate limit
     * @throws InvalidLogException if log input is invalid
     */
    @Transactional
    public DiagnosisResponse analyzeLogs(LogRequest logRequest, String ipAddress) {
        logger.info("Analyzing logs from IP: {}", ipAddress);

        // 1. Validate input
        validateLogInput(logRequest.getLogs());

        // 2. Check rate limit
        checkRateLimit(ipAddress);

        // 3. Analyze with AI
        String diagnosis = aiService.analyzeLog(logRequest.getLogs());
        String severity = aiService.determineSeverity(logRequest.getLogs());
        String title = aiService.extractTitle(logRequest.getLogs());

        // 4. Save to database
        AnalysisHistory history = saveAnalysis(ipAddress, logRequest.getLogs(), diagnosis, severity, title);

        // 5. Build response
        DiagnosisResponse response = DiagnosisResponse.builder()
                .severity(severity)
                .title(title)
                .content(diagnosis)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .analysisId(history.getId())
                .build();

        logger.info("Analysis completed successfully. ID: {}", history.getId());
        return response;
    }

    /**
     * Validate log input
     *
     * @param logs The log text to validate
     * @throws InvalidLogException if validation fails
     */
    private void validateLogInput(String logs) {
        if (logs == null || logs.trim().isEmpty()) {
            throw new InvalidLogException("Logs cannot be empty");
        }

        if (logs.trim().length() < 10) {
            throw new InvalidLogException("Logs are too short. Please provide more context (at least 10 characters)");
        }

        // Count words (approximate)
        int wordCount = logs.trim().split("\\s+").length;
        if (wordCount > 200) {
            throw new InvalidLogException(
                    String.format("Logs are too long (%d words). Please limit to 150 words or less", wordCount));
        }
    }

    /**
     * Check if user has exceeded rate limit
     *
     * @param ipAddress User's IP address
     * @throws RateLimitException if rate limit exceeded
     */
    private void checkRateLimit(String ipAddress) {
        LocalDateTime windowStart = LocalDateTime.now().minusHours(windowHours);

        Long requestCount = analysisHistoryRepository
                .countByIpAddressAndCreatedAtAfter(ipAddress, windowStart);

        logger.debug("IP {} has made {} requests in last {} hours",
                ipAddress, requestCount, windowHours);

        if (requestCount >= maxRequests) {
            logger.warn("Rate limit exceeded for IP: {}", ipAddress);
            throw new RateLimitException(maxRequests, windowHours);
        }
    }

    /**
     * Save analysis to database
     *
     * @param ipAddress User's IP
     * @param logInput Original log input
     * @param diagnosis AI diagnosis
     * @param severity Severity level
     * @param title Error title
     * @return Saved AnalysisHistory entity
     */
    private AnalysisHistory saveAnalysis(String ipAddress, String logInput,
                                         String diagnosis, String severity, String title) {
        AnalysisHistory history = AnalysisHistory.builder()
                .ipAddress(ipAddress)
                .logInput(logInput)
                .diagnosis(diagnosis)
                .severity(severity)
                .title(title)
                .build();

        return analysisHistoryRepository.save(history);
    }

    /**
     * Get remaining requests for an IP address
     *
     * @param ipAddress User's IP address
     * @return Number of requests remaining
     */
    public int getRemainingRequests(String ipAddress) {
        LocalDateTime windowStart = LocalDateTime.now().minusHours(windowHours);

        Long requestCount = analysisHistoryRepository
                .countByIpAddressAndCreatedAtAfter(ipAddress, windowStart);

        return Math.max(0, maxRequests - requestCount.intValue());
    }
}