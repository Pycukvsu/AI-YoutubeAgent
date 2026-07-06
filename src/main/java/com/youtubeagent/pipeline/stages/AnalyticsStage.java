package com.youtubeagent.pipeline.stages;

import com.youtubeagent.entity.AnalyticsSnapshot;
import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineStage;
import com.youtubeagent.pipeline.PipelineStageException;
import com.youtubeagent.repository.AnalyticsSnapshotRepository;
import com.youtubeagent.repository.VideoRepository;
import com.youtubeagent.service.YoutubeUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsStage.class);

    private final AnalyticsSnapshotRepository analyticsRepository;
    private final YoutubeUploadService youtubeUploadService;
    private final VideoRepository videoRepository;

    public AnalyticsStage(AnalyticsSnapshotRepository analyticsRepository,
                           YoutubeUploadService youtubeUploadService,
                           VideoRepository videoRepository) {
        this.analyticsRepository = analyticsRepository;
        this.youtubeUploadService = youtubeUploadService;
        this.videoRepository = videoRepository;
    }

    @Override
    public String name() {
        return "analytics";
    }

    @Override
    public void execute(PipelineContext context) throws PipelineStageException {
        try {
            Video video = context.getVideo();

            if (video.getYoutubeId() == null) {
                log.info("No YouTube ID, skipping analytics collection");
                return;
            }

            YoutubeUploadService.VideoStats stats = youtubeUploadService.getStats(video.getYoutubeId());

            AnalyticsSnapshot snapshot = new AnalyticsSnapshot(video, video.getYoutubeId());
            snapshot.setViews(stats.views());
            snapshot.setLikes(stats.likes());
            snapshot.setComments(stats.comments());

            analyticsRepository.save(snapshot);
            videoRepository.save(video);

            log.info("Analytics collected for video {}: {} views, {} likes",
                    video.getYoutubeId(), stats.views(), stats.likes());
        } catch (Exception e) {
            log.warn("Analytics collection failed (non-fatal): {}", e.getMessage());
        }
    }
}
