package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.entity.AbTest;
import com.youtubeagent.entity.Video;
import com.youtubeagent.entity.VideoAnalytics;
import com.youtubeagent.repository.AbTestRepository;
import com.youtubeagent.repository.VideoAnalyticsRepository;
import com.youtubeagent.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AbTestService {

    private static final Logger log = LoggerFactory.getLogger(AbTestService.class);

    private final AbTestRepository abTestRepository;
    private final VideoAnalyticsRepository analyticsRepository;
    private final VideoRepository videoRepository;
    private final ObjectMapper objectMapper;

    public AbTestService(AbTestRepository abTestRepository,
                         VideoAnalyticsRepository analyticsRepository,
                         VideoRepository videoRepository,
                         ObjectMapper objectMapper) {
        this.abTestRepository = abTestRepository;
        this.analyticsRepository = analyticsRepository;
        this.videoRepository = videoRepository;
        this.objectMapper = objectMapper;
    }

    public AbTest createTitleTest(String name, List<String> titleVariants) {
        AbTest test = new AbTest(name, "title");
        test.setDescription("A/B test for video titles: " + String.join(" vs ", titleVariants));

        try {
            Map<String, Object> variants = new HashMap<>();
            for (int i = 0; i < titleVariants.size(); i++) {
                variants.put("variant_" + (char)('A' + i), Map.of(
                        "title", titleVariants.get(i),
                        "videoIds", new ArrayList<>()
                ));
            }
            test.setVariantsJson(objectMapper.writeValueAsString(variants));
        } catch (Exception e) {
            log.error("Failed to serialize variants", e);
        }

        test = abTestRepository.save(test);
        log.info("Created A/B test: '{}' with {} variants", name, titleVariants.size());
        return test;
    }

    public AbTest createThumbnailTest(String name, List<String> thumbnailDescriptions) {
        AbTest test = new AbTest(name, "thumbnail");
        test.setDescription("A/B test for thumbnails: " + String.join(" vs ", thumbnailDescriptions));

        try {
            Map<String, Object> variants = new HashMap<>();
            for (int i = 0; i < thumbnailDescriptions.size(); i++) {
                variants.put("variant_" + (char)('A' + i), Map.of(
                        "description", thumbnailDescriptions.get(i),
                        "videoIds", new ArrayList<>()
                ));
            }
            test.setVariantsJson(objectMapper.writeValueAsString(variants));
        } catch (Exception e) {
            log.error("Failed to serialize variants", e);
        }

        test = abTestRepository.save(test);
        log.info("Created A/B test: '{}' with {} variants", name, thumbnailDescriptions.size());
        return test;
    }

    public AbTest createTimeTest(String name, List<String> times) {
        AbTest test = new AbTest(name, "timing");
        test.setDescription("A/B test for posting times: " + String.join(" vs ", times));

        try {
            Map<String, Object> variants = new HashMap<>();
            for (int i = 0; i < times.size(); i++) {
                variants.put("variant_" + (char)('A' + i), Map.of(
                        "time", times.get(i),
                        "videoIds", new ArrayList<>()
                ));
            }
            test.setVariantsJson(objectMapper.writeValueAsString(variants));
        } catch (Exception e) {
            log.error("Failed to serialize variants", e);
        }

        test = abTestRepository.save(test);
        log.info("Created A/B test: '{}' with {} variants", name, times.size());
        return test;
    }

    public String assignVariant(AbTest test) {
        try {
            JsonNode variants = objectMapper.readTree(test.getVariantsJson());
            Iterator<String> fieldNames = variants.fieldNames();

            String minVariant = null;
            int minCount = Integer.MAX_VALUE;

            while (fieldNames.hasNext()) {
                String variant = fieldNames.next();
                JsonNode variantNode = variants.path(variant);
                int count = variantNode.path("videoIds").size();
                if (count < minCount) {
                    minCount = count;
                    minVariant = variant;
                }
            }

            return minVariant;
        } catch (Exception e) {
            log.error("Failed to assign variant", e);
            return "variant_A";
        }
    }

    public void recordResult(Long testId, String variant, Long videoId) {
        try {
            AbTest test = abTestRepository.findById(testId).orElse(null);
            if (test == null) return;

            JsonNode variants = objectMapper.readTree(test.getVariantsJson());
            Map<String, Object> variantsMap = objectMapper.convertValue(variants, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Object> variantData = (Map<String, Object>) variantsMap.get(variant);
            if (variantData != null) {
                @SuppressWarnings("unchecked")
                List<Object> videoIds = (List<Object>) variantData.get("videoIds");
                if (!videoIds.contains(videoId)) {
                    videoIds.add(videoId);
                }
            }

            test.setVariantsJson(objectMapper.writeValueAsString(variantsMap));
            abTestRepository.save(test);

        } catch (Exception e) {
            log.error("Failed to record A/B test result", e);
        }
    }

    public Map<String, Object> analyzeTest(Long testId) {
        try {
            AbTest test = abTestRepository.findById(testId).orElse(null);
            if (test == null) return Map.of("error", "Test not found");

            JsonNode variants = objectMapper.readTree(test.getVariantsJson());
            Map<String, Object> results = new LinkedHashMap<>();
            results.put("testId", test.getId());
            results.put("name", test.getName());
            results.put("type", test.getTestType());
            results.put("status", test.getStatus());

            List<Map<String, Object>> variantResults = new ArrayList<>();
            String bestVariant = null;
            double bestScore = -1;

            Iterator<String> fieldNames = variants.fieldNames();
            while (fieldNames.hasNext()) {
                String variantName = fieldNames.next();
                JsonNode variantNode = variants.path(variantName);

                @SuppressWarnings("unchecked")
                List<Object> videoIds = objectMapper.convertValue(
                        variantNode.path("videoIds"), List.class);

                long totalViews = 0;
                long totalLikes = 0;
                int videoCount = videoIds.size();

                for (Object vid : videoIds) {
                    Long videoId = Long.valueOf(vid.toString());
                    List<VideoAnalytics> analytics = analyticsRepository.findByVideoIdOrderByRecordedAtDesc(videoId);
                    if (!analytics.isEmpty()) {
                        VideoAnalytics latest = analytics.get(0);
                        totalViews += latest.getViews();
                        totalLikes += latest.getLikes();
                    }
                }

                double avgViews = videoCount > 0 ? (double) totalViews / videoCount : 0;
                double engagementRate = totalViews > 0 ? (double) totalLikes / totalViews * 100 : 0;
                double score = avgViews * 0.7 + engagementRate * 0.3;

                Map<String, Object> variantResult = new LinkedHashMap<>();
                variantResult.put("name", variantName);
                variantResult.put("videoCount", videoCount);
                variantResult.put("totalViews", totalViews);
                variantResult.put("totalLikes", totalLikes);
                variantResult.put("avgViews", Math.round(avgViews));
                variantResult.put("engagementRate", Math.round(engagementRate * 100.0) / 100.0);
                variantResult.put("score", Math.round(score * 100.0) / 100.0);
                variantResults.add(variantResult);

                if (score > bestScore) {
                    bestScore = score;
                    bestVariant = variantName;
                }
            }

            results.put("variants", variantResults);
            results.put("winner", bestVariant);
            results.put("confidence", calculateConfidence(variantResults));

            return results;

        } catch (Exception e) {
            log.error("Failed to analyze A/B test", e);
            return Map.of("error", e.getMessage());
        }
    }

    private String calculateConfidence(List<Map<String, Object>> variantResults) {
        if (variantResults.size() < 2) return "insufficient_data";

        long totalVideos = variantResults.stream()
                .mapToLong(v -> (long) v.get("videoCount"))
                .sum();

        if (totalVideos < 10) return "low_sample_size";
        if (totalVideos < 30) return "moderate";
        return "high";
    }

    public List<AbTest> getActiveTests() {
        return abTestRepository.findByStatus("active");
    }

    public void completeTest(Long testId) {
        AbTest test = abTestRepository.findById(testId).orElse(null);
        if (test != null) {
            test.setStatus("completed");
            test.setCompletedAt(LocalDateTime.now());
            abTestRepository.save(test);
            log.info("A/B test '{}' completed", test.getName());
        }
    }
}
