package com.prodpulse.prodpulse_backend.controller;

import com.prodpulse.prodpulse_backend.model.dto.ApiKeyResponse;
import com.prodpulse.prodpulse_backend.model.entity.ApiKey;
import com.prodpulse.prodpulse_backend.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/generate")
    public ResponseEntity<ApiKeyResponse> generateKey(
            @RequestParam String name,
            @RequestParam ApiKey.KeyEnvironment environment) {
        return ResponseEntity.ok(apiKeyService.generateKey(name, environment));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getUserKeys() {
        return ResponseEntity.ok(apiKeyService.getUserKeys());
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<String> revokeKey(@PathVariable Long keyId) {
        apiKeyService.revokeKey(keyId);
        return ResponseEntity.ok("API key revoked successfully");
    }
}