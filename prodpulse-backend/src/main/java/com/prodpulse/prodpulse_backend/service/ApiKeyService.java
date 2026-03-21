package com.prodpulse.prodpulse_backend.service;

import com.prodpulse.prodpulse_backend.model.dto.ApiKeyResponse;
import com.prodpulse.prodpulse_backend.model.entity.ApiKey;
import com.prodpulse.prodpulse_backend.model.entity.User;
import com.prodpulse.prodpulse_backend.repository.ApiKeyRepository;
import com.prodpulse.prodpulse_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final UserRepository userRepository;

    public ApiKeyResponse generateKey(String name, ApiKey.KeyEnvironment environment) {
        User user = getCurrentUser();

        // Generate unique key: pp_live_<random>
        String rawKey = generateUniqueKey(environment);

        ApiKey apiKey = ApiKey.builder()
                .keyValue(rawKey)
                .name(name)
                .user(user)
                .environment(environment)
                .active(true)
                .build();

        apiKeyRepository.save(apiKey);

        return ApiKeyResponse.builder()
                .id(apiKey.getId())
                .name(apiKey.getName())
                .keyValue(rawKey) // shown only once
                .maskedKey(maskKey(rawKey))
                .environment(apiKey.getEnvironment())
                .active(apiKey.isActive())
                .createdAt(apiKey.getCreatedAt())
                .build();
    }

    public List<ApiKeyResponse> getUserKeys() {
        User user = getCurrentUser();
        return apiKeyRepository.findByUser(user)
                .stream()
                .map(key -> ApiKeyResponse.builder()
                        .id(key.getId())
                        .name(key.getName())
                        .maskedKey(maskKey(key.getKeyValue())) // never return full key again
                        .environment(key.getEnvironment())
                        .active(key.isActive())
                        .lastUsedAt(key.getLastUsedAt())
                        .createdAt(key.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public void revokeKey(Long keyId) {
        User user = getCurrentUser();
        ApiKey apiKey = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new RuntimeException("API key not found"));

        if (!apiKey.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
    }

    public ApiKey validateKey(String keyValue) {
        ApiKey apiKey = apiKeyRepository.findByKeyValueAndActiveTrue(keyValue)
                .orElseThrow(() -> new RuntimeException("Invalid or revoked API key"));

        // Update last used
        apiKey.setLastUsedAt(LocalDateTime.now());
        apiKeyRepository.save(apiKey);

        return apiKey;
    }

    private String generateUniqueKey(ApiKey.KeyEnvironment environment) {
        String prefix = environment == ApiKey.KeyEnvironment.PRODUCTION ? "pp_live_" : "pp_test_";
        String random;
        do {
            byte[] bytes = new byte[24];
            new SecureRandom().nextBytes(bytes);
            random = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (apiKeyRepository.existsByKeyValueAndActiveTrue(prefix + random));

        return prefix + random;
    }

    private String maskKey(String key) {
        if (key.length() < 12) return "****";
        return key.substring(0, 8) + "****" + key.substring(key.length() - 4);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}