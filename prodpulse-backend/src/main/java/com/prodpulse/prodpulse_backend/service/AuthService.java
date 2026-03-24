package com.prodpulse.prodpulse_backend.service;

import com.prodpulse.prodpulse_backend.model.dto.AuthRequest;
import com.prodpulse.prodpulse_backend.model.dto.AuthResponse;
import com.prodpulse.prodpulse_backend.model.dto.RegisterRequest;
import com.prodpulse.prodpulse_backend.model.dto.ResetPasswordRequest;
import com.prodpulse.prodpulse_backend.model.entity.User;
import com.prodpulse.prodpulse_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            User existing = userRepository.findByEmail(request.getEmail()).get();
            if (!existing.isEnabled()) {
                // Clean up stuck unverified user so they can re-register
                userRepository.delete(existing);
            } else {
                throw new RuntimeException("Email already registered");
            }
        }

        String otp = generateOtp();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .provider(User.AuthProvider.LOCAL)
                .enabled(false)
                .otp(otp)
                .otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        userRepository.save(user);

        try {
            emailService.sendOtpEmail(request.getEmail(), otp);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", request.getEmail(), e.getMessage());
            userRepository.delete(user); // rollback — don't leave dead user in DB
            throw new RuntimeException("Failed to send OTP email. Try again.");
        }

        return "OTP sent to " + request.getEmail();
    }

    public String verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new RuntimeException("Account already verified");
        }

        if (!user.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        user.setEnabled(true);
        user.setOtp(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);

        return jwtService.generateToken(user.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account not verified. Check your email for OTP");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getName(), user.getEmail());
    }

    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new RuntimeException("Account already verified");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            log.error("Failed to resend OTP to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to resend OTP. Try again.");
        }

        return "OTP resent to " + email;
    }

    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        if (user.getProvider() == User.AuthProvider.GOOGLE) {
            throw new RuntimeException("This account uses Google login. Please sign in with Google.");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiresAt(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        try {
            emailService.sendForgotPasswordEmail(user.getEmail(), otp);
        } catch (Exception e) {
            log.error("Failed to send forgot password email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send reset email. Try again.");
        }

        return "Password reset OTP sent to " + email;
    }

    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired. Please request a new one.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtp(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);

        return "Password reset successful. You can now login.";
    }

    private String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }
}