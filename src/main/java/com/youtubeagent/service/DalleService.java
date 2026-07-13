package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class DalleService {

    private static final Logger log = LoggerFactory.getLogger(DalleService.class);

    @Value("${dalle.api-key:}")
    private String apiKey;

    @Value("${dalle.model:dall-e-3}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DalleService(ObjectMapper objectMapper) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String generateImage(String prompt, String outputPath, String size) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "n", 1,
                    "size", size,
                    "response_format", "url"
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.openai.com/v1/images/generations",
                    HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            String imageUrl = root.path("data").get(0).path("url").asText();

            java.nio.file.Files.createDirectories(Path.of(outputPath).getParent());
            byte[] imageBytes = URI.create(imageUrl).toURL().openStream().readAllBytes();
            java.nio.file.Files.write(Path.of(outputPath), imageBytes);

            log.info("Generated image: {} ({} bytes)", outputPath, imageBytes.length);
            return outputPath;

        } catch (Exception e) {
            log.error("Image generation failed: {}", e.getMessage());
            throw new ExternalServiceException("DALL-E", e);
        }
    }

    public String generateSceneImage(String sceneDescription, String episodeTitle, int sceneNumber, String outputDir) {
        String prompt = "Cinematic scene for a short video: " + sceneDescription
                + ". Style: vibrant colors, dramatic lighting, vertical format 1080x1920, no text.";

        String outputPath = outputDir + "/scene_" + sceneNumber + ".png";
        return generateImage(prompt, outputPath, "1024x1792");
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.equals("demo");
    }
}
