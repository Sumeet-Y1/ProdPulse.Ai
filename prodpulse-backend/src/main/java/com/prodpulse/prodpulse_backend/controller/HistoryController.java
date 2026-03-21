package com.prodpulse.prodpulse_backend.controller;

import com.prodpulse.prodpulse_backend.model.entity.AnalysisHistory;
import com.prodpulse.prodpulse_backend.model.entity.User;
import com.prodpulse.prodpulse_backend.repository.AnalysisHistoryRepository;
import com.prodpulse.prodpulse_backend.repository.UserRepository;
import com.prodpulse.prodpulse_backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<AnalysisHistory>> getHistory(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AnalysisHistory> history = analysisHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId());

        return ResponseEntity.ok(history);
    }
}