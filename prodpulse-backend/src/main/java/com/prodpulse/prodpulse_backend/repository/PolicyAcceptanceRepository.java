package com.prodpulse.prodpulse_backend.repository;

import com.prodpulse.prodpulse_backend.model.entity.PolicyAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PolicyAcceptanceRepository extends JpaRepository<PolicyAcceptance, Long> {
    Optional<PolicyAcceptance> findByUserId(String userId);
    boolean existsByUserIdAndAcceptedTrue(String userId);
}