package com.prodpulse.prodpulse_backend.controller;

import com.prodpulse.prodpulse_backend.model.dto.PolicyAcceptanceRequest;
import com.prodpulse.prodpulse_backend.model.entity.PolicyAcceptance;
import com.prodpulse.prodpulse_backend.service.PolicyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptPolicy(
            @RequestBody PolicyAcceptanceRequest request,
            HttpServletRequest httpRequest) {

        if (!request.isAccepted()) {
            return ResponseEntity.status(403).body("You must accept the privacy policy to register.");
        }

        String ip = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        PolicyAcceptance saved = policyService.saveAcceptance(request, ip, userAgent);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/check/{userId}")
    public ResponseEntity<?> checkPolicy(@PathVariable String userId) {
        boolean accepted = policyService.hasAccepted(userId);
        return ResponseEntity.ok(accepted);
    }
}