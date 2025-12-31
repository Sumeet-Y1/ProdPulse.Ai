package com.prodpulse.prodpulse_backend.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

    @Autowired
    private GoogleGenAiChatModel chatModel;

    /**
     * Create ChatClient bean WITHOUT retry
     * This prevents multiple API calls from consuming quota
     */
    @Bean
    public ChatClient chatClient() {
        return ChatClient.builder(chatModel).build();
    }
}