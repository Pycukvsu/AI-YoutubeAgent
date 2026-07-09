package com.youtubeagent.service;

import com.youtubeagent.entity.Video;
import com.youtubeagent.entity.VideoAnalytics;
import com.youtubeagent.repository.VideoAnalyticsRepository;
import com.youtubeagent.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnhancedAnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(EnhancedAnalyticsService.class);

    private final VideoAnalyticsRepository analyticsRepository;
    private final VideoRepository videoRepository;
    private final YoutubeUploadService youtubeUploadService;

    public EnhancedAnalyticsService(VideoAnalyticsRepository analyticsRepository,
                                     VideoRepository videoRepository,
                                     YoutubeUploadService youtubeUploadService) {
        this.analyticsRepository = analyticsRepository;
        this.videoRepository = videoRepository;
        this.youtubeUploadService = youtubeUploadService;
    }

    public void collectAnalytics() {
        List<Video> uploadedVideos = videoRepository.findByStatus("uploaded");
        log.info("Collecting analytics for {} videos", uploadedVideos.size());

        for (Video video : uploadedVideos) {
            if (video.getYoutubeId() == null) continue;

            try {
                YoutubeUploadService.VideoStats stats = youtubeUploadService.getStats(video.getYoutubeId());

                VideoAnalytics analytics = new VideoAnalytics(video);
                analytics.setViews(stats.views());
                analytics.setLikes(stats.likes());
                analytics.setComments(stats.comments());
                analytics.setRecordedAt(LocalDateTime.now());

                analyticsRepository.save(analytics);
            } catch (Exception e) {
                log.warn("Failed to collect analytics for {}: {}", video.getYoutubeId(), e.getMessage());
            }
        }
    }

    public Map<String, Object> getVideoPerformance(Long videoId) {
        List<VideoAnalytics> history = analyticsRepository.findByVideoIdOrderByRecordedAtDesc(videoId);

        if (history.isEmpty()) {
            return Map.of("videoId", videoId, "status", "no_data");
        }

        VideoAnalytics latest = history.get(0);
        VideoAnalytics previous = history.size() > 1 ? history.get(1) : null;

        Map<String, Object> performance = new LinkedHashMap<>();
        performance.put("videoId", videoId);
        performance.put("current", Map.of(
                "views", latest.getViews(),
                "likes", latest.getLikes(),
                "comments", latest.getComments(),
                "recordedAt", latest.getRecordedAt()
        ));

        if (previous != null) {
            long viewsDelta = latest.getViews() - previous.getViews();
            long likesDelta = latest.getLikes() - previous.getLikes();
            performance.put("delta", Map.of(
                    "views", viewsDelta,
                    "likes", likesDelta,
                    "viewsGrowthRate", previous.getViews() > 0
                            ? Math.round((double) viewsDelta / previous.getViews() * 100 * 100.0) / 100.0
                            : 0
            ));
        }

        performance.put("trend", calculateTrend(history));
        performance.put("dataPoints", history.size());

        return performance;
    }

    public Map<String, Object> getChannelOverview() {
        List<Video> allVideos = videoRepository.findAll();
        List<Video> uploaded = allVideos.stream()
                .filter(v -> "uploaded".equals(v.getStatus()))
                .collect(Collectors.toList());

        long totalViews = 0;
        long totalLikes = 0;
        int totalComments = 0;

        for (Video video : uploaded) {
            List<VideoAnalytics> history = analyticsRepository.findByVideoIdOrderByRecordedAtDesc(video.getId());
            if (!history.isEmpty()) {
                VideoAnalytics latest = history.get(0);
                totalViews += latest.getViews();
                totalLikes += latest.getLikes();
                totalComments += latest.getComments();
            }
        }

        Map<String, Object> overview = new LinkedHashMap<>();
        overview.put("totalVideos", allVideos.size());
        overview.put("uploadedVideos", uploaded.size());
        overview.put("totalViews", totalViews);
        overview.put("totalLikes", totalLikes);
        overview.put("totalComments", totalComments);
        overview.put("avgViewsPerVideo", uploaded.isEmpty() ? 0 : totalViews / uploaded.size());
        overview.put("engagementRate", totalViews > 0
                ? Math.round((double) totalLikes / totalViews * 100 * 100.0) / 100.0
                : 0);

        return overview;
    }

    public List<Map<String, Object>> getTopPerformers(int limit) {
        List<Video> uploaded = videoRepository.findByStatus("uploaded");

        List<Map<String, Object>> performers = new ArrayList<>();

        for (Video video : uploaded) {
            List<VideoAnalytics> history = analyticsRepository.findByVideoIdOrderByRecordedAtDesc(video.getId());
            if (!history.isEmpty()) {
                VideoAnalytics latest = history.get(0);
                Map<String, Object> performer = new LinkedHashMap<>();
                performer.put("videoId", video.getId());
                performer.put("title", video.getTitle());
                performer.put("youtubeUrl", video.getYoutubeUrl());
                performer.put("views", latest.getViews());
                performer.put("likes", latest.getLikes());
                performer.put("engagementRate", latest.getViews() > 0
                        ? Math.round((double) latest.getLikes() / latest.getViews() * 100 * 100.0) / 100.0
                        : 0);
                performers.add(performer);
            }
        }

        performers.sort((a, b) -> Long.compare((long) b.get("views"), (long) a.get("views")));

        return performers.stream().limit(limit).collect(Collectors.toList());
    }

    public Map<String, Object> getBestPostingTimes() {
        List<VideoAnalytics> allAnalytics = analyticsRepository.findAll();

        Map<Integer, Long> viewsByHour = new TreeMap<>();
        Map<Integer, Long> countByHour = new TreeMap<>();

        for (VideoAnalytics analytics : allAnalytics) {
            int hour = analytics.getRecordedAt().getHour();
            viewsByHour.merge(hour, analytics.getViews(), Long::sum);
            countByHour.merge(hour, 1L, Long::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> hourlyStats = new ArrayList<>();

        for (int hour = 0; hour < 24; hour++) {
            long views = viewsByHour.getOrDefault(hour, 0L);
            long count = countByHour.getOrDefault(hour, 1L);
            double avgViews = (double) views / count;

            hourlyStats.add(Map.of(
                    "hour", hour,
                    "totalViews", views,
                    "videoCount", count,
                    "avgViews", Math.round(avgViews)
            ));
        }

        result.put("hourlyStats", hourlyStats);

        Optional<Map<String, Object>> bestHour = hourlyStats.stream()
                .max(Comparator.comparingDouble(s -> (double) s.get("avgViews")));

        result.put("bestHour", bestHour.map(s -> s.get("hour")).orElse(12));

        return result;
    }

    private String calculateTrend(List<VideoAnalytics> history) {
        if (history.size() < 2) return "insufficient_data";

        VideoAnalytics current = history.get(0);
        VideoAnalytics previous = history.get(1);

        long viewsDelta = current.getViews() - previous.getViews();
        double growthRate = previous.getViews() > 0
                ? (double) viewsDelta / previous.getViews() * 100
                : 0;

        if (growthRate > 10) return "rising";
        if (growthRate > 0) return "stable";
        if (growthRate > -10) return "declining";
        return "falling";
    }
}
