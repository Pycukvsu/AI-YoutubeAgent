package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.config.OpenAiConfig;
import com.youtubeagent.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class ThumbnailService {

    private static final Logger log = LoggerFactory.getLogger(ThumbnailService.class);

    private final OpenAiConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public ThumbnailService(OpenAiConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String generateThumbnail(String topic, String outputPath) {
        try {
            String prompt = """
                    Create a vibrant, eye-catching YouTube Shorts thumbnail for a video about: %s
                    Requirements:
                    - Vertical format (1080x1920)
                    - Bold, large text in Russian
                    - Bright colors, high contrast
                    - No real people faces
                    - Professional look
                    """.formatted(topic);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(config.getApiKey());

            Map<String, Object> body = Map.of(
                    "model", "dall-e-3",
                    "prompt", prompt,
                    "n", 1,
                    "size", "1024x1792",
                    "response_format", "url"
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getBaseUrl() + "/images/generations",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String imageUrl = root.path("data").get(0).path("url").asText();

            Path output = Paths.get(outputPath);
            Files.createDirectories(output.getParent());

            byte[] imageBytes = URI.create(imageUrl).toURL().openStream().readAllBytes();
            Files.write(output, imageBytes);

            log.info("Thumbnail generated: {} ({} bytes)", outputPath, imageBytes.length);
            return outputPath;

        } catch (Exception e) {
            log.error("Thumbnail generation failed: {}", e.getMessage());
            throw new ExternalServiceException("DALL-E", e);
        }
    }
}
