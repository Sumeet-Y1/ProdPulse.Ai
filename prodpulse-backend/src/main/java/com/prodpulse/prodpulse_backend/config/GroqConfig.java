package com.prodpulse.prodpulse_backend.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;

/**
 * Groq AI Configuration
 * Creates OpenAiChatModel bean for interacting with Groq API
 * Groq uses OpenAI-compatible API, so we use OpenAiChatModel
 */
@Configuration
public class GroqConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:llama-3.3-70b-versatile}")
    private String modelName;

    /**
     * Create OpenAiChatModel bean configured for Groq
     * This allows us to use Groq's LLaMA models via OpenAI-compatible API
     */
    @Bean
    public OpenAiChatModel openAiChatModel() {
        // Create OpenAiApi with base URL and API key
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();

        // Create chat options with default model
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(modelName)
                .temperature(0.3)
                .maxTokens(2000)
                .build();

        // Create a default ToolCallingManager
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();

        // Create and return OpenAiChatModel with all required parameters
        return new OpenAiChatModel(
                openAiApi,
                options,
                toolCallingManager,
                RetryTemplate.builder().maxAttempts(3).build(),
                ObservationRegistry.NOOP
        );
    }
}