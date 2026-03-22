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
            @RequestBody Map<String, Object> body,
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

        // 3. Extract sentAt from context for deduplication
        String sentAt = null;
        try {
            Object contextObj = body.get("context");
            if (contextObj instanceof Map) {
                Object sentAtObj = ((Map<?, ?>) contextObj).get("sentAt");
                if (sentAtObj != null) {
                    sentAt = sentAtObj.toString();
                }
            }
        } catch (Exception ignored) {}

        // 4. Deduplication check — if same sentAt already processed, return success silently
        if (sentAt != null) {
            boolean alreadyProcessed = analysisHistoryRepository
                    .existsByUserIdAndSentAt(user.getId(), sentAt);
            if (alreadyProcessed) {
                return ResponseEntity.ok(Map.of(
                        "status", "duplicate",
                        "message", "Log already processed"
                ));
            }
        }

        // 5. Check and reset daily counter if new day
        LocalDateTime now = LocalDateTime.now();
        if (user.getLastResetDate() == null ||
                user.getLastResetDate().toLocalDate().isBefore(now.toLocalDate())) {
            user.setDailyAnalysesCount(0);
            user.setLastResetDate(now);
            userRepository.save(user);
        }

        // 6. Check daily limit
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

        // 7. Extract logs
        String logs = (String) body.get("logs");
        if (logs == null || logs.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Logs cannot be empty"));
        }

        // 8. Analyze with AI
        String diagnosis;
        try {
            diagnosis = aiService.analyzeLog(logs);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI analysis failed. Try again."));
        }

        // 9. Determine severity and title
        String severity = aiService.determineSeverity(logs);
        String title = aiService.extractTitle(logs);

        // 10. Increment daily counter
        user.setDailyAnalysesCount(user.getDailyAnalysesCount() + 1);
        userRepository.save(user);

        // 11. Save to history with sentAt
        AnalysisHistory history = AnalysisHistory.builder()
                .userId(user.getId())
                .ipAddress(request.getRemoteAddr())
                .logInput(logs)
                .diagnosis(diagnosis)
                .severity(severity)
                .title(title)
                .sentAt(sentAt)
                .build();
        analysisHistoryRepository.save(history);

        // 12. Broadcast via WebSocket
        Long userId = user.getId();
        webSocketService.broadcastDiagnosis(userId, diagnosis, severity, title);

        // 13. If critical — send alert
        if ("critical".equals(severity)) {
            webSocketService.broadcastCriticalAlert(userId, title,
                    "Critical error detected in your production system. Check your dashboard immediately.");
        }

        // 14. Return diagnosis with usage info
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