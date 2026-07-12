package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.config.YoutubeConfig;
import com.youtubeagent.exception.ExternalServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class YoutubeUploadService {

    private static final Logger log = LoggerFactory.getLogger(YoutubeUploadService.class);

    private final YoutubeConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public YoutubeUploadService(YoutubeConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public String getAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(
                    "client_id=" + config.getClientId() +
                    "&client_secret=" + config.getClientSecret() +
                    "&refresh_token=" + config.getRefreshToken() +
                    "&grant_type=refresh_token",
                    headers
            );

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://oauth2.googleapis.com/token",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("access_token").asText();

        } catch (Exception e) {
            throw new ExternalServiceException("YouTube OAuth", e);
        }
    }

    public UploadResult upload(String filePath, String title, String description, String[] tags) {
        try {
            String accessToken = getAccessToken();
            log.info("Uploading video '{}' to YouTube", title);

            // Note: YouTube multipart upload requires resumable upload protocol
            // This is a simplified version. For production, use YouTube Java client library.
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> metadata = Map.of(
                    "snippet", Map.of(
                            "title", title,
                            "description", description != null ? description : "",
                            "tags", tags != null ? java.util.List.of(tags) : java.util.List.of(),
                            "categoryId", config.getDefaultCategory()
                    ),
                    "status", Map.of(
                            "privacyStatus", config.getDefaultVisibility().toLowerCase(),
                            "selfDeclaredMadeForKids", false
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(metadata, headers);

            // Initialize resumable upload
            ResponseEntity<String> initResponse = restTemplate.exchange(
                    "https://www.googleapis.com/upload/youtube/v3/videos?uploadType=resumable&part=snippet,status",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String uploadUrl = initResponse.getHeaders().getLocation().toString();

            // Upload the actual video file
            java.io.File videoFile = new java.io.File(filePath);
            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            uploadHeaders.setContentLength(videoFile.length());
            uploadHeaders.set("Content-Range", "bytes 0-" + (videoFile.length() - 1) + "/" + videoFile.length());

            byte[] videoBytes = java.nio.file.Files.readAllBytes(videoFile.toPath());
            HttpEntity<byte[]> uploadEntity = new HttpEntity<>(videoBytes, uploadHeaders);

            ResponseEntity<String> uploadResponse = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.PUT,
                    uploadEntity,
                    String.class
            );

            JsonNode responseRoot = objectMapper.readTree(uploadResponse.getBody());
            String videoId = responseRoot.path("id").asText();

            log.info("Video uploaded successfully: youtube.com/shorts/{}", videoId);
            return new UploadResult(videoId, "https://youtube.com/shorts/" + videoId);

        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("YouTube upload failed: {}", e.getMessage());
            throw new ExternalServiceException("YouTube Upload", e);
        }
    }

    public VideoStats getStats(String youtubeVideoId) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://www.googleapis.com/youtube/v3/videos?part=statistics&id=" + youtubeVideoId,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");

            if (items.isArray() && items.size() > 0) {
                JsonNode stats = items.get(0).path("statistics");
                return new VideoStats(
                        Long.parseLong(stats.path("viewCount").asText("0")),
                        Long.parseLong(stats.path("likeCount").asText("0")),
                        Integer.parseInt(stats.path("commentCount").asText("0"))
                );
            }

            return new VideoStats(0, 0, 0);

        } catch (Exception e) {
            log.warn("Failed to get stats for {}: {}", youtubeVideoId, e.getMessage());
            return new VideoStats(0, 0, 0);
        }
    }

    public record UploadResult(String videoId, String url) {}
    public record VideoStats(long views, long likes, int comments) {}
}
