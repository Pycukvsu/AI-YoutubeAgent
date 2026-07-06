package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.config.PexelsConfig;
import com.youtubeagent.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PexelsService {

    private static final Logger log = LoggerFactory.getLogger(PexelsService.class);

    private final PexelsConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public PexelsService(PexelsConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public List<VideoClip> searchVideos(String query, int count) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", config.getApiKey());

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    config.getBaseUrl() + "/videos/search?query=" + query + "&per_page=" + count + "&size=small",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode videos = root.path("videos");

            List<VideoClip> clips = new ArrayList<>();
            for (JsonNode video : videos) {
                String id = video.path("id").asText();
                int duration = video.path("duration").asInt();

                if (duration < config.getMinDurationSeconds()) {
                    continue;
                }

                JsonNode videoFiles = video.path("video_files");
                String downloadUrl = null;
                int minWidth = Integer.MAX_VALUE;

                for (JsonNode file : videoFiles) {
                    int width = file.path("width").asInt();
                    int height = file.path("height").asInt();
                    String fileType = file.path("file_type").asText();

                    if ("video/mp4".equals(fileType) && height >= 1080 && width < minWidth) {
                        downloadUrl = file.path("link").asText();
                        minWidth = width;
                    }
                }

                if (downloadUrl == null && videoFiles.size() > 0) {
                    downloadUrl = videoFiles.get(0).path("link").asText();
                }

                if (downloadUrl != null) {
                    clips.add(new VideoClip(id, downloadUrl, duration));
                }
            }

            log.info("Found {} video clips for query '{}'", clips.size(), query);
            return clips;

        } catch (Exception e) {
            log.error("Pexels search failed: {}", e.getMessage());
            throw new ExternalServiceException("Pexels", e);
        }
    }

    public String downloadClip(VideoClip clip, String outputDir) {
        try {
            Path dir = Paths.get(outputDir);
            Files.createDirectories(dir);
            Path outputPath = dir.resolve("clip_" + clip.id() + ".mp4");

            try (InputStream in = URI.create(clip.downloadUrl()).toURL().openStream()) {
                Files.copy(in, outputPath);
            }

            log.info("Downloaded clip {} to {}", clip.id(), outputPath);
            return outputPath.toAbsolutePath().toString();

        } catch (Exception e) {
            log.error("Failed to download clip {}: {}", clip.id(), e.getMessage());
            throw new ExternalServiceException("Pexels", e);
        }
    }

    public record VideoClip(String id, String downloadUrl, int durationSeconds) {}
}
