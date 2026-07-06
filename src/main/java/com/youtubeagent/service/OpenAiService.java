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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAiService(OpenAiConfig config, ObjectMapper objectMapper) {
        this.config = config;
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

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            Map<String, Object> body = Map.of(
                    "model", config.getModel(),
                    "messages", new Object[]{
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    },
                    "temperature", config.getTemperature(),
                    "max_tokens", config.getMaxTokens(),
                    "response_format", Map.of("type", "json_object")
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getBaseUrl() + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
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

            int tokensUsed = root.path("usage").path("total_tokens").asInt(0);
            log.info("Generated script for topic '{}', tokens used: {}", request.getTopic(), tokensUsed);

            return result;

        } catch (Exception e) {
            log.error("Failed to generate script: {}", e.getMessage());
            throw new ExternalServiceException("OpenAI", e);
        }
    }

    public Map<String, Object> callOpenAi(String prompt, int maxTokens) {
        try {
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
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            int tokensUsed = root.path("usage").path("total_tokens").asInt(0);

            return Map.of("content", content, "tokensUsed", tokensUsed);

        } catch (Exception e) {
            log.error("OpenAI call failed: {}", e.getMessage());
            throw new ExternalServiceException("OpenAI", e);
        }
    }
}
