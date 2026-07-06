package com.youtubeagent.pipeline.stages;

import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineContext;
import com.youtubeagent.pipeline.PipelineStage;
import com.youtubeagent.pipeline.PipelineStageException;
import com.youtubeagent.service.FfmpegService;
import com.youtubeagent.util.FileUtils;
import com.youtubeagent.util.SubtitleGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class VideoAssemblyStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(VideoAssemblyStage.class);

    private final FfmpegService ffmpegService;
    private final SubtitleGenerator subtitleGenerator;

    @Value("${pipeline.temp-dir:/tmp/youtube-agent}")
    private String tempDir;

    public VideoAssemblyStage(FfmpegService ffmpegService, SubtitleGenerator subtitleGenerator) {
        this.ffmpegService = ffmpegService;
        this.subtitleGenerator = subtitleGenerator;
    }

    @Override
    public String name() {
        return "video_assembly";
    }

    @Override
    public void execute(PipelineContext context) throws PipelineStageException {
        try {
            Video video = context.getVideo();
            String videoTempDir = Paths.get(tempDir, "video_" + video.getId()).toString();
            String clipsDir = Paths.get(videoTempDir, "clips").toString();

            Path finalOutput = Paths.get(videoTempDir, "final.mp4");
            Files.createDirectories(finalOutput.getParent());

            String srtPath = null;
            if (context.getScript() != null && context.getAudioFilePath() != null) {
                String srtContent = subtitleGenerator.generateSrt(
                        context.getScript().getContent(),
                        getAudioDurationSeconds(context.getAudioFilePath())
                );
                srtPath = Paths.get(videoTempDir, "subtitles.srt").toString();
                Files.writeString(Path.of(srtPath), srtContent);
                context.setSubtitleFile(srtPath);
            }

            String musicPath = null;
            try {
                ClassPathResource musicResource = new ClassPathResource("music/background.mp3");
                if (musicResource.exists()) {
                    musicPath = Paths.get(videoTempDir, "background.mp3").toString();
                    Files.copy(musicResource.getInputStream(), Path.of(musicPath), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                log.warn("No background music found, proceeding without music");
            }

            ffmpegService.assembleFull(clipsDir, context.getAudioFilePath(), srtPath, musicPath, finalOutput.toString());

            context.setFinalVideoPath(finalOutput.toString());
            video.setFilePath(finalOutput.toString());
            video.setStatus("assembled");

            log.info("Video assembled: {}", finalOutput);
        } catch (PipelineStageException e) {
            throw e;
        } catch (Exception e) {
            throw new PipelineStageException(name(), e);
        }
    }

    private double getAudioDurationSeconds(String audioPath) {
        try {
            String duration = ffmpegService.getAudioDuration(audioPath);
            return Double.parseDouble(duration);
        } catch (Exception e) {
            log.warn("Could not determine audio duration, defaulting to 45s");
            return 45.0;
        }
    }
}
