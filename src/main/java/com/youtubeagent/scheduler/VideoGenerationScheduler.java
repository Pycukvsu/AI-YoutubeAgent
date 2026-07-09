package com.youtubeagent.scheduler;

import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineOrchestrator;
import com.youtubeagent.repository.VideoRepository;
import com.youtubeagent.service.TrendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VideoGenerationScheduler {

    private static final Logger log = LoggerFactory.getLogger(VideoGenerationScheduler.class);

    private final PipelineOrchestrator pipelineOrchestrator;
    private final VideoRepository videoRepository;
    private final TrendService trendService;

    public VideoGenerationScheduler(PipelineOrchestrator pipelineOrchestrator,
                                     VideoRepository videoRepository,
                                     TrendService trendService) {
        this.pipelineOrchestrator = pipelineOrchestrator;
        this.videoRepository = videoRepository;
        this.trendService = trendService;
    }

    @Scheduled(cron = "${scheduling.video-cron:0 0 10,14,18 * * *}")
    public void generateVideos() {
        log.info("Scheduled video generation started");

        long availableTrends = trendService.getAvailableTrendsCount();
        if (availableTrends == 0) {
            log.info("No available trends, skipping generation");
            return;
        }

        int pendingVideos = videoRepository.findByStatus("pending").size();
        if (pendingVideos > 0) {
            log.info("Already {} pending videos, skipping generation", pendingVideos);
            return;
        }

        Video video = new Video("Scheduled generation", null);
        video = videoRepository.save(video);

        try {
            pipelineOrchestrator.execute(video);
        } catch (Exception e) {
            log.error("Scheduled video generation failed: {}", e.getMessage());
        }
    }
}
