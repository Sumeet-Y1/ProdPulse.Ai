package com.prodpulse.prodpulse_backend.service;

import com.prodpulse.prodpulse_backend.model.dto.PolicyAcceptanceRequest;
import com.prodpulse.prodpulse_backend.model.entity.PolicyAcceptance;
import com.prodpulse.prodpulse_backend.repository.PolicyAcceptanceRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PolicyService {

    private final PolicyAcceptanceRepository repository;

    public PolicyService(PolicyAcceptanceRepository repository) {
        this.repository = repository;
    }

    public PolicyAcceptance saveAcceptance(PolicyAcceptanceRequest request, String ip, String userAgent) {
        if (!request.isAccepted()) {
            throw new RuntimeException("User must accept the privacy policy to register.");
        }

        PolicyAcceptance policy = new PolicyAcceptance();
        policy.setUserId(request.getUserId());
        policy.setEmail(request.getEmail());
        policy.setAccepted(true);
        policy.setPolicyVersion(
                request.getPolicyVersion() != null ? request.getPolicyVersion() : "v1.0"
        );
        policy.setAcceptedAt(LocalDateTime.now());
        policy.setIp(ip);
        policy.setUserAgent(userAgent);

        return repository.save(policy);
    }

    public boolean hasAccepted(String userId) {
        return repository.existsByUserIdAndAcceptedTrue(userId);
    }
}