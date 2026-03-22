package com.prodpulse.prodpulse_backend.repository;

import com.prodpulse.prodpulse_backend.model.entity.AnalysisHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AnalysisHistory entity
 * Spring Data JPA automatically implements these methods!
 */
@Repository
public interface AnalysisHistoryRepository extends JpaRepository<AnalysisHistory, Long> {

    /**
     * Count analyses by IP address within a time window (for rate limiting)
     */
    Long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);

    /**
     * Find all analyses by IP address (for history)
     */
    List<AnalysisHistory> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

    /**
     * Find recent analyses by IP (for debugging)
     */
    List<AnalysisHistory> findByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);

    /**
     * Find all analyses by user ID
     */
    List<AnalysisHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Check if a log with same sentAt timestamp already exists (deduplication)
     */
    boolean existsByUserIdAndSentAt(Long userId, String sentAt);

}