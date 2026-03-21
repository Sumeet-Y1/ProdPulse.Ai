package com.prodpulse.prodpulse_backend.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Value("${groq.api.key1}")
    private String apiKey1;

    @Value("${groq.api.key2}")
    private String apiKey2;

    @Value("${groq.api.key3}")
    private String apiKey3;

    @Value("${spring.ai.openai.chat.options.temperature:0.3}")
    private Double temperature;

    @Value("${spring.ai.openai.chat.options.max-tokens:2000}")
    private Integer maxTokens;

    // FREE tier fallback chain (best to worst, highest tokens/day first)
    private static final List<String[]> FREE_CHAIN = List.of(
            new String[]{"llama-3.1-8b-instant", "key1"},      // 500K tokens/day
            new String[]{"allam-2-7b", "key1"},                  // 500K tokens/day
            new String[]{"llama-3.1-8b-instant", "key2"},       // another 500K
            new String[]{"allam-2-7b", "key2"},                  // another 500K
            new String[]{"llama-3.1-8b-instant", "key3"},       // another 500K
            new String[]{"groq/compound-mini", "key1"}           // unlimited fallback
    );

    // STARTER ₹999 fallback chain
    private static final List<String[]> STARTER_CHAIN = List.of(
            new String[]{"qwen/qwen3-32b", "key1"},              // 500K tokens/day
            new String[]{"meta-llama/llama-4-scout-17b-16e-instruct", "key1"}, // 500K tokens/day
            new String[]{"qwen/qwen3-32b", "key2"},              // another 500K
            new String[]{"meta-llama/llama-4-scout-17b-16e-instruct", "key2"}, // another 500K
            new String[]{"qwen/qwen3-32b", "key3"},              // another 500K
            new String[]{"groq/compound", "key1"}                // unlimited fallback
    );

    // PRO ₹2,999 fallback chain
    private static final List<String[]> PRO_CHAIN = List.of(
            new String[]{"moonshotai/kimi-k2-instruct", "key1"}, // 300K tokens/day best quality
            new String[]{"openai/gpt-oss-120b", "key1"},         // 200K tokens/day
            new String[]{"moonshotai/kimi-k2-instruct", "key2"}, // another 300K
            new String[]{"openai/gpt-oss-120b", "key2"},         // another 200K
            new String[]{"moonshotai/kimi-k2-instruct", "key3"}, // another 300K
            new String[]{"qwen/qwen3-32b", "key1"}               // fallback to starter quality
    );

    @PostConstruct
    public void init() {
        logger.info("=== PRODPULSE AI SERVICE ===");
        logger.info("Key 1 present: {}", apiKey1 != null && apiKey1.length() > 10);
        logger.info("Key 2 present: {}", apiKey2 != null && apiKey2.length() > 10);
        logger.info("Key 3 present: {}", apiKey3 != null && apiKey3.length() > 10);
        logger.info("FREE chain: {} models", FREE_CHAIN.size());
        logger.info("STARTER chain: {} models", STARTER_CHAIN.size());
        logger.info("PRO chain: {} models", PRO_CHAIN.size());
        logger.info("===========================");
    }

    private static final String SYSTEM_PROMPT = """
            You are ProdPulse.AI, the world's most advanced production error diagnostic system,
            used by software companies to instantly understand and fix production failures.
            
            You are like a senior engineer with 20 years of experience across every tech stack,
            who can look at any error log and immediately know what went wrong and exactly how to fix it.
            
            YOUR CAPABILITIES:
            You can analyze logs from ANY system including:
            - Languages: Node.js, Java, Python, Go, Ruby, PHP, .NET, Rust, Kotlin, Swift
            - Databases: MySQL, PostgreSQL, MongoDB, Redis, Cassandra, DynamoDB, Supabase
            - Cloud: AWS, GCP, Azure, Railway, Render, Heroku, Vercel, Netlify, Fly.io
            - Containers: Docker, Kubernetes, ECS, EKS, GKE
            - Frameworks: Spring Boot, Django, Express, Laravel, Rails, FastAPI, NestJS, Next.js
            - Message queues: Kafka, RabbitMQ, SQS
            - Mobile backends, microservices, monoliths, serverless functions
            
            YOUR RULES:
            - NEVER say "I cannot analyze this" — always give your best diagnosis
            - If the log is unclear or incomplete, state your assumptions clearly
            - Always include exact code snippets for fixes when relevant
            - Be direct and actionable — developers are busy and under pressure
            - Explain in plain English so non-technical managers can understand too
            - If it is a CRITICAL error, make it very clear at the top
            - Always give an estimated time to fix
            
            FORMAT YOUR RESPONSE AS HTML:
            
            <div class="diagnosis">
            
                <div class="severity-badge severity-{critical|warning|info}">
                    🚨 CRITICAL | ⚠️ WARNING | ℹ️ INFO
                </div>
            
                <h3>🔍 What Happened:</h3>
                <p>Clear explanation of root cause in simple terms.</p>
            
                <h3>🎯 Root Cause:</h3>
                <p>Technical deep dive into exactly why this error occurred.</p>
            
                <h3>🔧 How to Fix:</h3>
                <ol>
                    <li>
                        <strong>Step 1: Title of action</strong>
                        <p>Explanation of what to do</p>
                        <pre><code>// Code snippet if applicable</code></pre>
                    </li>
                </ol>
            
                <h3>💡 Prevention Tips:</h3>
                <ul>
                    <li><strong>Tip 1:</strong> Explanation</li>
                </ul>
            
                <h3>⏱️ Estimated Fix Time:</h3>
                <p>How long this should realistically take to fix</p>
            
                <h3>🔗 Related Issues to Check:</h3>
                <ul>
                    <li>Other things that might be affected or related</li>
                </ul>
            
            </div>
            """;

    /**
     * Analyze log for FREE tier users
     */
    public String analyzeLog(String errorLog) {
        return analyzeWithChain(errorLog, FREE_CHAIN);
    }

    /**
     * Analyze log based on user plan
     */
    public String analyzeLogByPlan(String errorLog, String plan) {
        return switch (plan.toUpperCase()) {
            case "PRO", "ENTERPRISE" -> analyzeWithChain(errorLog, PRO_CHAIN);
            case "STARTER" -> analyzeWithChain(errorLog, STARTER_CHAIN);
            default -> analyzeWithChain(errorLog, FREE_CHAIN);
        };
    }

    /**
     * Try each model in the chain until one succeeds
     */
    private String analyzeWithChain(String errorLog, List<String[]> chain) {
        for (String[] modelConfig : chain) {
            String model = modelConfig[0];
            String keyName = modelConfig[1];
            String key = resolveKey(keyName);

            try {
                logger.info("Trying model: {} with key: {}", model, keyName);
                String result = analyzeWithModel(errorLog, model, key);
                logger.info("Success with model: {}", model);
                return result;
            } catch (Exception e) {
                if (isRateLimitError(e)) {
                    logger.warn("Rate limit hit for model: {}, key: {}. Trying next...", model, keyName);
                } else {
                    logger.error("Error with model: {}, key: {}. Error: {}", model, keyName, e.getMessage());
                }
            }
        }

        // All models exhausted
        logger.error("All models in chain exhausted!");
        return generateFallbackResponse(errorLog);
    }

    /**
     * Call specific model with specific API key
     */
    private String analyzeWithModel(String errorLog, String model, String key) {
        String fullPrompt = SYSTEM_PROMPT
                + "\n\nAnalyze this production error log and provide a complete diagnosis:\n\n"
                + "```\n" + errorLog + "\n```"
                + "\n\nProvide your full HTML diagnosis now:";

        // Build a new OpenAiApi with the specific key
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl("https://api.groq.com/openai")
                .apiKey(key)
                .build();

        OpenAiChatModel model1 = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .build();

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(model)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();

        Prompt prompt = new Prompt(List.of(new UserMessage(fullPrompt)), chatOptions);
        ChatResponse response = model1.call(prompt);

        String diagnosis = response.getResult().getOutput().getText();

        // Clean markdown if AI wraps in code blocks
        diagnosis = diagnosis
                .replaceAll("```html\\n?", "")
                .replaceAll("```\\n?", "")
                .trim();

        return diagnosis;
    }

    private String resolveKey(String keyName) {
        return switch (keyName) {
            case "key2" -> apiKey2;
            case "key3" -> apiKey3;
            default -> apiKey1;
        };
    }

    private boolean isRateLimitError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return false;
        return msg.contains("429") ||
                msg.contains("rate limit") ||
                msg.contains("rate_limit") ||
                msg.contains("quota") ||
                msg.contains("too many requests");
    }

    public String determineSeverity(String errorLog) {
        String logLower = errorLog.toLowerCase();

        if (logLower.contains("fatal") ||
                logLower.contains("outofmemoryerror") ||
                logLower.contains("out of memory") ||
                logLower.contains("cannot connect") ||
                logLower.contains("connection refused") ||
                logLower.contains("segmentation fault") ||
                logLower.contains("core dumped") ||
                logLower.contains("system crash") ||
                logLower.contains("kernel panic") ||
                logLower.contains("disk full") ||
                logLower.contains("no space left") ||
                logLower.contains("deadlock") ||
                logLower.contains("nullpointerexception") ||
                logLower.contains("stackoverflow")) {
            return "critical";
        }

        if (logLower.contains("error") ||
                logLower.contains("exception") ||
                logLower.contains("failed") ||
                logLower.contains("timeout") ||
                logLower.contains("unauthorized") ||
                logLower.contains("forbidden") ||
                logLower.contains("not found") ||
                logLower.contains("deprecated") ||
                logLower.contains("retry")) {
            return "warning";
        }

        return "info";
    }

    public String extractTitle(String errorLog) {
        String[] lines = errorLog.split("\n");

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.toLowerCase().contains("exception") ||
                    trimmed.toLowerCase().contains("fatal") ||
                    trimmed.toLowerCase().contains("error")) {
                if (trimmed.length() > 100) trimmed = trimmed.substring(0, 97) + "...";
                return trimmed;
            }
        }

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.length() > 100) trimmed = trimmed.substring(0, 97) + "...";
                return trimmed;
            }
        }

        return "Production Error Analysis";
    }

    private String generateFallbackResponse(String errorLog) {
        logger.warn("Generating fallback response — all models exhausted");

        return """
                <div class="diagnosis">
                    <div class="severity-badge severity-warning">⚠️ WARNING</div>
                    
                    <h3>⚠️ AI Service Temporarily Unavailable</h3>
                    <p>All AI models are temporarily rate limited. Please try again in a few minutes.</p>
                    
                    <h3>🔍 Your Error Log:</h3>
                    <pre><code>%s</code></pre>
                    
                    <h3>🔧 Common Fixes to Try:</h3>
                    <ol>
                        <li><strong>Check environment variables</strong><p>Ensure all required env vars are set correctly.</p></li>
                        <li><strong>Verify database connection</strong><p>Check your connection string and credentials.</p></li>
                        <li><strong>Check dependencies</strong><p>Run your package manager install command.</p></li>
                        <li><strong>Review deployment logs</strong><p>Check your hosting platform for more context.</p></li>
                        <li><strong>Check resource limits</strong><p>Ensure memory, CPU, disk limits aren't exceeded.</p></li>
                    </ol>
                    
                    <h3>⏱️ Estimated Fix Time:</h3>
                    <p>Please try again in a few minutes for AI-powered diagnosis.</p>
                </div>
                """.formatted(errorLog.length() > 500 ? errorLog.substring(0, 500) + "..." : errorLog);
    }
}