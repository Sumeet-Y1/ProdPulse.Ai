package com.prodpulse.prodpulse_backend.controller;

import com.prodpulse.prodpulse_backend.model.entity.ApiKey;
import com.prodpulse.prodpulse_backend.model.entity.AnalysisHistory;
import com.prodpulse.prodpulse_backend.model.entity.User;
import com.prodpulse.prodpulse_backend.repository.AnalysisHistoryRepository;
import com.prodpulse.prodpulse_backend.repository.UserRepository;
import com.prodpulse.prodpulse_backend.service.AIService;
import com.prodpulse.prodpulse_backend.service.ApiKeyService;
import com.prodpulse.prodpulse_backend.service.WebSocketService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogIngestionController {

    private final ApiKeyService apiKeyService;
    private final AIService aiService;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final WebSocketService webSocketService;
    private final UserRepository userRepository;

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestLogs(
            @RequestHeader("X-API-Key") String apiKeyValue,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        // 1. Validate API key
        ApiKey apiKey;
        try {
            apiKey = apiKeyService.validateKey(apiKeyValue);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid or revoked API key"));
        }

        // 2. Get user
        User user = apiKey.getUser();

        // 3. Check and reset daily counter if new day
        LocalDateTime now = LocalDateTime.now();
        if (user.getLastResetDate() == null ||
                user.getLastResetDate().toLocalDate().isBefore(now.toLocalDate())) {
            user.setDailyAnalysesCount(0);
            user.setLastResetDate(now);
            userRepository.save(user);
        }

        // 4. Check daily limit
        if (user.getDailyAnalysesCount() >= user.getDailyLimit()) {
            return ResponseEntity.status(429)
                    .body(Map.of(
                            "error", "Daily analysis limit reached",
                            "plan", user.getPlan().toString(),
                            "limit", user.getDailyLimit(),
                            "used", user.getDailyAnalysesCount(),
                            "resetsAt", now.toLocalDate().plusDays(1).toString(),
                            "upgradeUrl", "https://prodpulse.ai/pricing"
                    ));
        }

        // 5. Extract logs
        String logs = body.get("logs");
        if (logs == null || logs.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Logs cannot be empty"));
        }

        // 6. Analyze with AI
        String diagnosis;
        try {
            diagnosis = aiService.analyzeLog(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI analysis failed. Try again."));
        }

        // 7. Determine severity and title
        String severity = aiService.determineSeverity(logs);
        String title = aiService.extractTitle(logs);

        // 8. Increment daily counter
        user.setDailyAnalysesCount(user.getDailyAnalysesCount() + 1);
        userRepository.save(user);

        // 9. Save to history
        AnalysisHistory history = AnalysisHistory.builder()
                .userId(user.getId())
                .ipAddress(request.getRemoteAddr())
                .logInput(logs)
                .diagnosis(diagnosis)
                .severity(severity)
                .title(title)
                .build();
        analysisHistoryRepository.save(history);

        // 10. Broadcast via WebSocket
        Long userId = user.getId();
        webSocketService.broadcastDiagnosis(userId, diagnosis, severity, title);

        // 11. If critical — send alert
        if ("critical".equals(severity)) {
            webSocketService.broadcastCriticalAlert(userId, title,
                    "Critical error detected in your production system. Check your dashboard immediately.");
        }

        // 12. Return diagnosis with usage info
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "diagnosis", diagnosis,
                "severity", severity,
                "title", title,
                "usage", Map.of(
                        "used", user.getDailyAnalysesCount(),
                        "limit", user.getDailyLimit(),
                        "remaining", user.getDailyLimit() - user.getDailyAnalysesCount(),
                        "plan", user.getPlan().toString()
                )
        ));
    }
}