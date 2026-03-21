package com.prodpulse.prodpulse_backend.model.dto;

import com.prodpulse.prodpulse_backend.model.entity.ApiKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyResponse {

    private Long id;
    private String name;
    private String keyValue; // shown only once on creation
    private String maskedKey; // pp_live_****...xk29 for display
    private ApiKey.KeyEnvironment environment;
    private boolean active;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
}