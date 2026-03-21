package com.prodpulse.prodpulse_backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(nullable = false)
    private boolean enabled;

    @Column
    private String otp;

    @Column
    private LocalDateTime otpExpiresAt;

    // Plan field for rate limiting
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Plan plan = Plan.FREE;

    // Daily analyses counter
    @Column(nullable = false)
    @Builder.Default
    private int dailyAnalysesCount = 0;

    // Last reset date for daily counter
    @Column
    private LocalDateTime lastResetDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastResetDate = LocalDateTime.now();
    }

    // UserDetails methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get daily limit based on plan
     */
    public int getDailyLimit() {
        return switch (plan) {
            case FREE -> 50;
            case APP -> 500;
            case PRO -> 1000;
            case ENTERPRISE -> Integer.MAX_VALUE;
        };
    }

    /**
     * Check if user has exceeded daily limit
     */
    public boolean hasExceededDailyLimit() {
        // Reset counter if it's a new day
        if (lastResetDate == null ||
                lastResetDate.toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
            return false; // will be reset before checking
        }
        return dailyAnalysesCount >= getDailyLimit();
    }

    public enum AuthProvider {
        LOCAL, GOOGLE
    }

    public enum Plan {
        FREE, APP, PRO, ENTERPRISE
    }
}