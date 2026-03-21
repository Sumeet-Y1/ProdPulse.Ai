package com.prodpulse.prodpulse_backend.controller;

import com.prodpulse.prodpulse_backend.model.dto.AuthRequest;
import com.prodpulse.prodpulse_backend.model.dto.AuthResponse;
import com.prodpulse.prodpulse_backend.model.dto.RegisterRequest;
import com.prodpulse.prodpulse_backend.model.dto.ForgotPasswordRequest;
import com.prodpulse.prodpulse_backend.model.dto.ResetPasswordRequest;
import com.prodpulse.prodpulse_backend.model.entity.User;
import com.prodpulse.prodpulse_backend.repository.UserRepository;
import com.prodpulse.prodpulse_backend.service.AuthService;
import com.prodpulse.prodpulse_backend.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {
        String token = authService.verifyOtp(email, otp);
        return ResponseEntity.ok(new AuthResponse(token, null, email));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestParam String email) {
        return ResponseEntity.ok(authService.resendOtp(email));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
        ));
    }

    @GetMapping("/usage")
    public ResponseEntity<?> getUsage(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Reset counter if new day
        LocalDateTime now = LocalDateTime.now();
        if (user.getLastResetDate() == null ||
                user.getLastResetDate().toLocalDate().isBefore(now.toLocalDate())) {
            user.setDailyAnalysesCount(0);
            user.setLastResetDate(now);
            userRepository.save(user);
        }

        int used = user.getDailyAnalysesCount();
        int limit = user.getDailyLimit();
        int remaining = limit - used;
        int usagePercent = limit > 0 ? (used * 100) / limit : 0;

        return ResponseEntity.ok(Map.of(
                "plan", user.getPlan().toString(),
                "used", used,
                "limit", limit,
                "remaining", remaining,
                "usagePercent", usagePercent,
                "resetsAt", now.toLocalDate().plusDays(1).toString()
        ));
    }
}