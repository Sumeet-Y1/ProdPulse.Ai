package com.prodpulse.prodpulse_backend.service;

import com.prodpulse.prodpulse_backend.model.dto.DiagnosisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Broadcast log diagnosis to a specific user's dashboard
     * @param userId - the user whose dashboard should receive the update
     * @param diagnosis - AI diagnosis result
     * @param severity - critical, warning, info
     * @param title - brief error title
     */
    public void broadcastDiagnosis(Long userId, String diagnosis, String severity, String title) {
        Map<String, Object> payload = Map.of(
                "type", "NEW_DIAGNOSIS",
                "userId", userId,
                "title", title,
                "diagnosis", diagnosis,
                "severity", severity,
                "timestamp", LocalDateTime.now().toString()
        );

        // Send to user specific channel
        messagingTemplate.convertAndSend("/topic/diagnosis/" + userId, payload);
    }

    /**
     * Broadcast critical alert to user
     */
    public void broadcastCriticalAlert(Long userId, String title, String message) {
        Map<String, Object> payload = Map.of(
                "type", "CRITICAL_ALERT",
                "userId", userId,
                "title", title,
                "message", message,
                "timestamp", LocalDateTime.now().toString()
        );

        messagingTemplate.convertAndSend("/topic/alerts/" + userId, payload);
    }
}