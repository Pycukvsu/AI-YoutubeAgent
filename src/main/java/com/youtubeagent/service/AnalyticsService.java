package com.youtubeagent.service;

import com.youtubeagent.dto.AnalyticsDto;
import com.youtubeagent.entity.AnalyticsSnapshot;
import com.youtubeagent.entity.Video;
import com.youtubeagent.repository.AnalyticsSnapshotRepository;
import com.youtubeagent.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final AnalyticsSnapshotRepository analyticsRepository;
    private final VideoRepository videoRepository;
    private final YoutubeUploadService youtubeUploadService;

    public AnalyticsService(AnalyticsSnapshotRepository analyticsRepository,
                             VideoRepository videoRepository,
                             YoutubeUploadService youtubeUploadService) {
        this.analyticsRepository = analyticsRepository;
        this.videoRepository = videoRepository;
        this.youtubeUploadService = youtubeUploadService;
    }

    public List<AnalyticsDto> getVideoAnalytics(Long videoId) {
        return analyticsRepository.findByVideoIdOrderBySnapshotAtDesc(videoId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public void refreshAllAnalytics() {
        List<Video> uploadedVideos = videoRepository.findByStatus("uploaded");
        log.info("Refreshing analytics for {} videos", uploadedVideos.size());

        for (Video video : uploadedVideos) {
            if (video.getYoutubeId() == null) continue;

            try {
                YoutubeUploadService.VideoStats stats = youtubeUploadService.getStats(video.getYoutubeId());
                AnalyticsSnapshot snapshot = new AnalyticsSnapshot(video, video.getYoutubeId());
                snapshot.setViews(stats.views());
                snapshot.setLikes(stats.likes());
                snapshot.setComments(stats.comments());
                analyticsRepository.save(snapshot);
            } catch (Exception e) {
                log.warn("Failed to refresh analytics for video {}: {}", video.getYoutubeId(), e.getMessage());
            }
        }
    }

    private AnalyticsDto toDto(AnalyticsSnapshot snapshot) {
        AnalyticsDto dto = new AnalyticsDto();
        dto.setVideoId(snapshot.getVideo().getId());
        dto.setYoutubeVideoId(snapshot.getYoutubeVideoId());
        dto.setViews(snapshot.getViews());
        dto.setLikes(snapshot.getLikes());
        dto.setComments(snapshot.getComments());
        dto.setSubscribersGained(snapshot.getSubscribersGained());
        dto.setWatchTimeMinutes(snapshot.getWatchTimeMinutes());
        dto.setSnapshotAt(snapshot.getSnapshotAt());
        return dto;
    }
}
