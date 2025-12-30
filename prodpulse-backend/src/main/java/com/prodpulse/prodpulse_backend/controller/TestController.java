package com.prodpulse.prodpulse_backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
public class TestController {

    @Value("${spring.ai.google.genai.api-key}")
    private String apiKey;

    @GetMapping("/test-gemini-models")
    public String testGeminiModels() {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // Test v1 API
            HttpRequest requestV1 = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1/models?key=" + apiKey))
                    .GET()
                    .build();

            HttpResponse<String> responseV1 = client.send(requestV1, HttpResponse.BodyHandlers.ofString());

            // Test v1beta API
            HttpRequest requestV1Beta = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey))
                    .GET()
                    .build();

            HttpResponse<String> responseV1Beta = client.send(requestV1Beta, HttpResponse.BodyHandlers.ofString());

            return "=== V1 API Models ===\n" + responseV1.body() +
                    "\n\n=== V1Beta API Models ===\n" + responseV1Beta.body();

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}