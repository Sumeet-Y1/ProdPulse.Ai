package com.prodpulse.prodpulse_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for interacting with Google Gemini AI
 * Handles log analysis using Google GenAI (Gemini)
 */
@Service
public class AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIService.class);

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    /**
     * System prompt for Gemini - defines how AI should analyze logs
     */
    private static final String SYSTEM_PROMPT = """
            You are ProdPulse.AI, an expert production error diagnostic system.
            You specialize in analyzing error logs from production environments,
            particularly Railway, Docker, Node.js, MySQL, PostgreSQL, and common web frameworks.
            
            Your role:
            1. Analyze the error log provided
            2. Identify the root cause
            3. Provide clear, actionable solutions
            4. Suggest prevention strategies
            
            Format your response as HTML with these sections:
            
            <div class="diagnosis">
                <h3>🔍 What Happened:</h3>
                <p>Brief explanation of the root cause in simple terms</p>
                
                <h3>🔧 How to Fix:</h3>
                <ul>
                    <li>Step 1: Specific action</li>
                    <li>Step 2: Another action</li>
                    <li>Step 3: Final action</li>
                </ul>
                
                <h3>💡 Prevention Tips:</h3>
                <ul>
                    <li>Best practice 1</li>
                    <li>Best practice 2</li>
                </ul>
            </div>
            
            Focus on:
            - Railway deployment issues
            - Environment variable problems
            - Database connection errors
            - Memory/CPU issues (OOM)
            - Port binding problems
            - Docker container issues
            - Common Node.js/Java/Python errors
            
            Keep explanations clear and actionable. Avoid jargon when possible.
            """;

    /**
     * Analyze production error logs using Google Gemini
     *
     * @param errorLog The error log text to analyze
     * @return AI-generated diagnosis in HTML format
     */
    public String analyzeLog(String errorLog) {
        logger.info("Starting log analysis with Google Gemini");

        try {
            // Create chat client
            ChatClient chatClient = chatClientBuilder.build();

            // Create user prompt with the error log
            String userPrompt = """
                    Analyze this production error log and provide diagnosis:
                    
                    {errorLog}
                    
                    Remember to format your response as HTML as specified in the system instructions.
                    """;

            // Create prompt template
            PromptTemplate promptTemplate = new PromptTemplate(SYSTEM_PROMPT + "\n\n" + userPrompt);
            Prompt prompt = promptTemplate.create(Map.of("errorLog", errorLog));

            // Call Gemini API
            logger.debug("Calling Google Gemini API...");
            String response = chatClient.prompt(prompt).call().content();

            logger.info("Successfully received diagnosis from Gemini");
            return response;

        } catch (Exception e) {
            logger.error("Error calling Google Gemini API: {}", e.getMessage(), e);

            // Fallback response if AI fails
            return generateFallbackResponse(errorLog);
        }
    }

    /**
     * Determine severity level from error log
     *
     * @param errorLog The error log text
     * @return Severity level: "critical", "warning", or "info"
     */
    public String determineSeverity(String errorLog) {
        String logLower = errorLog.toLowerCase();

        // Critical errors
        if (logLower.contains("fatal") ||
                logLower.contains("outofmemoryerror") ||
                logLower.contains("cannot connect") ||
                logLower.contains("connection refused") ||
                logLower.contains("segmentation fault") ||
                logLower.contains("core dumped")) {
            return "critical";
        }

        // Warning level
        if (logLower.contains("error") ||
                logLower.contains("exception") ||
                logLower.contains("failed") ||
                logLower.contains("timeout")) {
            return "warning";
        }

        // Info level
        return "info";
    }

    /**
     * Extract title from error log (first error line or summary)
     *
     * @param errorLog The error log text
     * @return Brief title for the error
     */
    public String extractTitle(String errorLog) {
        String[] lines = errorLog.split("\n");

        // Find first line with "Error" or "Exception"
        for (String line : lines) {
            if (line.toLowerCase().contains("error") ||
                    line.toLowerCase().contains("exception")) {
                // Clean up and limit length
                String title = line.trim();
                if (title.length() > 100) {
                    title = title.substring(0, 97) + "...";
                }
                return title;
            }
        }

        // Fallback: use first non-empty line
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                if (trimmed.length() > 100) {
                    trimmed = trimmed.substring(0, 97) + "...";
                }
                return trimmed;
            }
        }

        return "Production Error Analysis";
    }

    /**
     * Generate fallback response if AI service fails
     *
     * @param errorLog The error log text
     * @return Basic HTML diagnosis
     */
    private String generateFallbackResponse(String errorLog) {
        logger.warn("Generating fallback response (AI service unavailable)");

        return """
                <div class="diagnosis">
                    <h3>⚠️ AI Service Temporarily Unavailable</h3>
                    <p>We're unable to process your log with AI right now, but here's what we can tell you:</p>
                    
                    <h3>🔍 Your Error Log:</h3>
                    <pre style="background: #f5f5f5; padding: 10px; border-radius: 5px; overflow-x: auto;">%s</pre>
                    
                    <h3>💡 Common Solutions:</h3>
                    <ul>
                        <li>Check your environment variables are correctly set</li>
                        <li>Verify database connection strings</li>
                        <li>Ensure all dependencies are installed</li>
                        <li>Check Railway/deployment logs for more context</li>
                        <li>Verify memory and CPU limits aren't exceeded</li>
                    </ul>
                    
                    <p>Please try again in a few moments. If the issue persists, contact support.</p>
                </div>
                """.formatted(errorLog.length() > 500 ? errorLog.substring(0, 500) + "..." : errorLog);
    }
}