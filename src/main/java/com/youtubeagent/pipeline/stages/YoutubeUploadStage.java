package com.youtubeagent.pipeline.stages;

import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineStage;
import com.youtubeagent.pipeline.PipelineStageException;
import com.youtubeagent.service.VideoQualityService;
import com.youtubeagent.service.YoutubeUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class YoutubeUploadStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(YoutubeUploadStage.class);

    private final YoutubeUploadService youtubeUploadService;
    private final VideoQualityService videoQualityService;

    public YoutubeUploadStage(YoutubeUploadService youtubeUploadService,
                               VideoQualityService videoQualityService) {
        this.youtubeUploadService = youtubeUploadService;
        this.videoQualityService = videoQualityService;
    }

    @Override
    public String name() {
        return "youtube_upload";
    }

    @Override
    public void execute(PipelineContext context) throws PipelineStageException {
        try {
            Video video = context.getVideo();

            if (video.getFilePath() == null) {
                throw new PipelineStageException(name(), "No video file to upload");
            }

            VideoQualityService.QualityResult quality = videoQualityService.checkVideo(video.getFilePath());
            if (!quality.valid()) {
                log.warn("Video quality check failed: {}", quality.message());
                throw new PipelineStageException(name(), "Video quality check failed: " + quality.message());
            }

            log.info("Video quality OK: {}s, {}x{}, {}MB",
                    String.format("%.1f", quality.durationSeconds()),
                    quality.width(), quality.height(), quality.fileSizeMB());

            YoutubeUploadService.UploadResult result = youtubeUploadService.upload(
                    video.getFilePath(),
                    video.getTitle(),
                    video.getDescription(),
                    video.getTags()
            );

            video.setYoutubeId(result.videoId());
            video.setYoutubeUrl("https://youtube.com/shorts/" + result.videoId());
            video.setStatus("uploaded");
            video.setUploadedAt(java.time.LocalDateTime.now());

            context.setYoutubeVideoId(result.videoId());

            log.info("Video uploaded to YouTube: {}", video.getYoutubeUrl());
        } catch (PipelineStageException e) {
            throw e;
        } catch (Exception e) {
            throw new PipelineStageException(name(), e);
        }
    }
}
