package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.config.OpenAiConfig;
import com.youtubeagent.dto.ScriptRequest;
import com.youtubeagent.dto.ScriptResponse;
import com.youtubeagent.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OpenAiService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    private final OpenAiConfig config;
    private final GeminiService geminiService;
    private final GroqService groqService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiService(OpenAiConfig config, GeminiService geminiService, GroqService groqService, ObjectMapper objectMapper) {
        this.config = config;
        this.geminiService = geminiService;
        this.groqService = groqService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public ScriptResponse generateScript(ScriptRequest request) {
        String systemPrompt = """
                Ты — профессиональный сценарист для коротких видео (YouTube Shorts, 30-60 секунд).
                Пиши на русском языке.
                Формат ответа — строго JSON:
                {
                    "script_text": "текст сценария для озвучки",
                    "title": "заголовок видео (макс 100 символов)",
                    "description": "описание для YouTube (2-3 предложения)",
                    "tags": ["тег1", "тег2", "тег3", "тег4", "тег5"],
                    "duration_estimate": 45
                }
                Правила:
                - Сценарий должен быть 30-60 секунд при чтении вслух
                - Используй цепляющие первые 3 секунды
                - Заканчивай call-to-action (подписка, лайк)
                - Не используй markdown, только чистый JSON
                """;

        String userPrompt = """
                Тема: %s
                Тон: %s
                Целевая длительность: %d секунд
                """.formatted(request.getTopic(), request.getTone(), request.getTargetDurationSeconds());

        String fullPrompt = systemPrompt + "\n\n" + userPrompt;

        try {
            String content = callAi(fullPrompt, 800);
            JsonNode scriptJson = objectMapper.readTree(content);

            ScriptResponse result = new ScriptResponse();
            result.setScriptText(scriptJson.path("script_text").asText());
            result.setTitle(scriptJson.path("title").asText());
            result.setDescription(scriptJson.path("description").asText());
            result.setDurationEstimateSeconds(scriptJson.path("duration_estimate").asInt(45));

            JsonNode tagsNode = scriptJson.path("tags");
            if (tagsNode.isArray()) {
                String[] tags = new String[tagsNode.size()];
                for (int i = 0; i < tagsNode.size(); i++) {
                    tags[i] = tagsNode.get(i).asText();
                }
                result.setTags(tags);
            }

            log.info("Generated script for topic '{}'", request.getTopic());
            return result;

        } catch (Exception e) {
            log.error("Failed to generate script: {}", e.getMessage());
            throw new ExternalServiceException("AI", e);
        }
    }

    public Map<String, Object> callOpenAi(String prompt, int maxTokens) {
        try {
            String content = callAi(prompt, maxTokens);
            return Map.of("content", content, "tokensUsed", 0);
        } catch (Exception e) {
            log.error("AI call failed: {}", e.getMessage());
            throw new ExternalServiceException("AI", e);
        }
    }

    private String callAi(String prompt, int maxTokens) throws Exception {
        if (groqService.isConfigured()) {
            log.info("Using Groq API");
            Map<String, Object> result = groqService.callGroq(prompt, maxTokens);
            return (String) result.get("content");
        }

        if (geminiService.isConfigured()) {
            log.info("Using Gemini API");
            Map<String, Object> result = geminiService.callGemini(prompt, maxTokens);
            return (String) result.get("content");
        }

        if (config.getApiKey() != null && !config.getApiKey().isBlank()
                && !config.getApiKey().equals("demo")) {
            log.info("Using OpenAI API");
            return callOpenAiDirect(prompt, maxTokens);
        }

        throw new ExternalServiceException("AI", "No AI provider configured. Set GROQ_API_KEY, GEMINI_API_KEY, or OPENAI_API_KEY.");
    }

    private String callOpenAiDirect(String prompt, int maxTokens) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        Map<String, Object> body = Map.of(
                "model", config.getModel(),
                "messages", new Object[]{
                        Map.of("role", "user", "content", prompt)
                },
                "temperature", config.getTemperature(),
                "max_tokens", maxTokens,
                "response_format", Map.of("type", "json_object")
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                config.getBaseUrl() + "/chat/completions",
                HttpMethod.POST, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("choices").get(0).path("message").path("content").asText();
    }
}
