package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.config.TrendDiscoveryConfig;
import com.youtubeagent.entity.Trend;
import com.youtubeagent.exception.ExternalServiceException;
import com.youtubeagent.repository.TrendRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TrendDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(TrendDiscoveryService.class);

    private final TrendDiscoveryConfig config;
    private final OpenAiService openAiService;
    private final YoutubeUploadService youtubeUploadService;
    private final GoogleTrendsService googleTrendsService;
    private final TrendRepository trendRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public TrendDiscoveryService(TrendDiscoveryConfig config,
                                  OpenAiService openAiService,
                                  YoutubeUploadService youtubeUploadService,
                                  GoogleTrendsService googleTrendsService,
                                  TrendRepository trendRepository,
                                  ObjectMapper objectMapper) {
        this.config = config;
        this.openAiService = openAiService;
        this.youtubeUploadService = youtubeUploadService;
        this.googleTrendsService = googleTrendsService;
        this.trendRepository = trendRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    public int discoverTrends() {
        int totalNew = 0;

        if (config.isEnableOpenAiGeneration()) {
            totalNew += discoverFromOpenAi();
        }

        if (config.isEnableYoutubeSearch()) {
            totalNew += discoverFromYouTube();
        }

        if (config.isEnableGoogleTrends()) {
            totalNew += discoverFromGoogleTrends();
        }

        log.info("Trend discovery complete: {} new trends found", totalNew);
        return totalNew;
    }

    private int discoverFromOpenAi() {
        try {
            String categoriesStr = String.join(", ", config.getCategories());

            String prompt = """
                    Придумай %d трендовых тем для YouTube Shorts на русском языке на эту неделю.
                    Категории: %s

                    Формат ответа — строго JSON:
                    {
                        "topics": [
                            {
                                "topic": "название темы на русском",
                                "category": "категория из списка",
                                "reason": "почему это тренд (1 предложение)",
                                "viral_score": 8
                            }
                        ]
                    }

                    Правила:
                    - Темы должны быть интересными для широкой аудитории
                    - Каждая тема должна быть адаптирована под формат Shorts (30-60 сек)
                    - Оценивай виральность от 1 до 10
                    - Не повторяй очевидные темы
                    - Используй актуальные события и факты
                    """.formatted(config.getTopicsPerRun(), categoriesStr);

            Map<String, Object> response = openAiService.callOpenAi(prompt, 800);
            String content = (String) response.get("content");
            int tokensUsed = (int) response.get("tokensUsed");

            JsonNode root = objectMapper.readTree(content);
            JsonNode topics = root.path("topics");

            int newCount = 0;
            for (JsonNode topicNode : topics) {
                String topic = topicNode.path("topic").asText();
                String category = topicNode.path("category").asText();
                int viralScore = topicNode.path("viral_score").asInt(5);

                if (viralScore < config.getMinViralScore()) {
                    log.debug("Skipping '{}' with viral score {} (min: {})", topic, viralScore, config.getMinViralScore());
                    continue;
                }

                if (findOrCreateTrend(topic, "openai", category)) {
                    newCount++;
                }
            }

            log.info("OpenAI trend discovery: {} new trends ({} tokens)", newCount, tokensUsed);
            return newCount;

        } catch (Exception e) {
            log.error("OpenAI trend discovery failed: {}", e.getMessage());
            return 0;
        }
    }

    private int discoverFromYouTube() {
        try {
            String accessToken = youtubeUploadService.getAccessToken();
            if (accessToken == null || accessToken.isBlank()) {
                log.warn("No YouTube access token, skipping YouTube trend discovery");
                return 0;
            }

            String categoriesStr = String.join(" OR ", config.getCategories());
            String searchQuery = "shorts " + categoriesStr;

            String url = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet"
                    + "&q=" + java.net.URLEncoder.encode(searchQuery, java.nio.charset.StandardCharsets.UTF_8)
                    + "&type=video"
                    + "&videoDuration=short"
                    + "&order=viewCount"
                    + "&regionCode=RU"
                    + "&maxResults=20";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");

            int newCount = 0;
            Set<String> seenTopics = new HashSet<>();

            for (JsonNode item : items) {
                JsonNode snippet = item.path("snippet");
                String title = snippet.path("title").asText();
                String description = snippet.path("description").asText();

                String topic = extractTopicFromTitle(title, description);
                if (topic != null && !seenTopics.contains(topic.toLowerCase())) {
                    seenTopics.add(topic.toLowerCase());
                    if (findOrCreateTrend(topic, "youtube_trending", "auto")) {
                        newCount++;
                    }
                }
            }

            log.info("YouTube trend discovery: {} new trends", newCount);
            return newCount;

        } catch (Exception e) {
            log.error("YouTube trend discovery failed: {}", e.getMessage());
            return 0;
        }
    }

    private String extractTopicFromTitle(String title, String description) {
        if (title == null || title.isBlank()) return null;

        String cleaned = title
                .replaceAll("(?i)#Shorts|#shorts|#SHORTS", "")
                .replaceAll("[\\p{So}\\p{Cn}]", "")
                .trim();

        if (cleaned.length() < 5 || cleaned.length() > 100) return null;

        return cleaned;
    }

    private int discoverFromGoogleTrends() {
        try {
            String topicsRaw = googleTrendsService.fetchTrends(config.getLanguage().toUpperCase(), config.getTopicsPerRun());
            String[] topics = topicsRaw.split("\n");

            int newCount = 0;
            for (String topic : topics) {
                topic = topic.trim();
                if (!topic.isEmpty() && findOrCreateTrend(topic, "google_trends", "trending")) {
                    newCount++;
                }
            }

            log.info("Google Trends discovery: {} new trends", newCount);
            return newCount;

        } catch (Exception e) {
            log.error("Google Trends discovery failed: {}", e.getMessage());
            return 0;
        }
    }

    public boolean findOrCreateTrend(String topic, String source, String category) {
        Optional<Trend> existing = trendRepository.findByTopicIgnoreCaseAndStatus(topic, "new");

        if (existing.isPresent()) {
            log.debug("Trend already exists: '{}'", topic);
            return false;
        }

        Trend trend = new Trend(topic, source);
        trend.setCategory(category);
        trend.setDiscoveredAt(LocalDateTime.now());
        trendRepository.save(trend);
        log.info("New trend discovered: '{}' (source: {}, category: {})", topic, source, category);
        return true;
    }
}
