package com.youtubeagent.pipeline.stages;

import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineStage;
import com.youtubeagent.pipeline.PipelineStageException;
import com.youtubeagent.service.PexelsService;
import com.youtubeagent.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class StockVideoSelectionStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(StockVideoSelectionStage.class);

    private final PexelsService pexelsService;

    @Value("${pipeline.temp-dir:/tmp/youtube-agent}")
    private String tempDir;

    @Value("${pexels.clips-per-video:4}")
    private int clipsPerVideo;

    public StockVideoSelectionStage(PexelsService pexelsService) {
        this.pexelsService = pexelsService;
    }

    @Override
    public String name() {
        return "stock_video_selection";
    }

    @Override
    public void execute(PipelineContext context) throws PipelineStageException {
        try {
            Video video = context.getVideo();
            String topic = context.getTrend() != null ? context.getTrend().getTopic() : "nature";
            String clipsDir = Paths.get(tempDir, "video_" + video.getId(), "clips").toString();
            FileUtils.ensureDir(clipsDir);

            List<PexelsService.VideoClip> clips = pexelsService.searchVideos(topic, clipsPerVideo);

            if (clips.isEmpty()) {
                clips = pexelsService.searchVideos("abstract", clipsPerVideo);
            }

            List<String> downloadedPaths = new ArrayList<>();
            for (PexelsService.VideoClip clip : clips) {
                String path = pexelsService.downloadClip(clip, clipsDir);
                downloadedPaths.add(path);
            }

            context.setVideoClips(downloadedPaths);
            log.info("Downloaded {} video clips to {}", downloadedPaths.size(), clipsDir);
        } catch (PipelineStageException e) {
            throw e;
        } catch (Exception e) {
            throw new PipelineStageException(name(), e);
        }
    }
}
