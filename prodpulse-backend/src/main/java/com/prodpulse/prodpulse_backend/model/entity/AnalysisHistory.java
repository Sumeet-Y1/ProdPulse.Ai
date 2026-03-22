package com.prodpulse.prodpulse_backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "log_input", columnDefinition = "TEXT", nullable = false)
    private String logInput;

    @Column(name = "diagnosis", columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "severity", length = 20)
    private String severity;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "sent_at", length = 50)
    private String sentAt; // SDK server timestamp for deduplication

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}