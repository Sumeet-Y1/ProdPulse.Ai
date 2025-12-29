package com.prodpulse.prodpulse_backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing analysis history in database
 * Tracks all log analyses for rate limiting and history
 */
@Entity
@Table(name = "analysis_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * IP address of the user (for rate limiting)
     */
    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    /**
     * Original log input from user
     */
    @Column(name = "log_input", columnDefinition = "TEXT", nullable = false)
    private String logInput;

    /**
     * AI-generated diagnosis
     */
    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    /**
     * Severity level: critical, warning, info
     */
    @Column(name = "severity", length = 20)
    private String severity;

    /**
     * Brief title of the issue
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * Timestamp when analysis was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}