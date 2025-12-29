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
     *
     * @param ipAddress User's IP address
     * @param since Start time for counting
     * @return Number of analyses in the time window
     */
    Long countByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);

    /**
     * Find all analyses by IP address (for history)
     *
     * @param ipAddress User's IP address
     * @return List of all analyses by this IP
     */
    List<AnalysisHistory> findByIpAddressOrderByCreatedAtDesc(String ipAddress);

    /**
     * Find recent analyses by IP (for debugging)
     *
     * @param ipAddress User's IP address
     * @param since Start time
     * @return Recent analyses
     */
    List<AnalysisHistory> findByIpAddressAndCreatedAtAfter(String ipAddress, LocalDateTime since);

}