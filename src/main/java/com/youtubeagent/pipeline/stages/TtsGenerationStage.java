package com.youtubeagent.pipeline.stages;

import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineStage;
import com.youtubeagent.pipeline.PipelineStageException;
import com.youtubeagent.service.EdgeTtsService;
import com.youtubeagent.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class TtsGenerationStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(TtsGenerationStage.class);

    private final EdgeTtsService edgeTtsService;

    @Value("${pipeline.temp-dir:/tmp/youtube-agent}")
    private String tempDir;

    public TtsGenerationStage(EdgeTtsService edgeTtsService) {
        this.edgeTtsService = edgeTtsService;
    }

    @Override
    public String name() {
        return "tts_generation";
    }

    @Override
    public void execute(PipelineContext context) throws PipelineStageException {
        try {
            Video video = context.getVideo();
            String videoTempDir = Paths.get(tempDir, "video_" + video.getId()).toString();
            FileUtils.ensureDir(videoTempDir);

            String audioPath = Paths.get(videoTempDir, "voiceover.mp3").toString();

            edgeTtsService.generateAudio(context.getScript().getContent(), audioPath);

            context.setAudioFilePath(audioPath);
            log.info("TTS audio generated: {}", audioPath);
        } catch (PipelineStageException e) {
            throw e;
        } catch (Exception e) {
            throw new PipelineStageException(name(), e);
        }
    }
}
