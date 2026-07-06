package com.youtubeagent.pipeline;

import com.youtubeagent.entity.PipelineRun;
import com.youtubeagent.entity.Video;
import com.youtubeagent.repository.PipelineRunRepository;
import com.youtubeagent.repository.VideoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class PipelineOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PipelineOrchestrator.class);

    private final List<PipelineStage> stages;
    private final PipelineRunRepository pipelineRunRepository;
    private final VideoRepository videoRepository;
    private final ObjectMapper objectMapper;

    @Value("${pipeline.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${pipeline.retry.backoff-ms:5000}")
    private long backoffMs;

    public PipelineOrchestrator(List<PipelineStage> stages,
                                 PipelineRunRepository pipelineRunRepository,
                                 VideoRepository videoRepository,
                                 ObjectMapper objectMapper) {
        this.stages = stages;
        this.pipelineRunRepository = pipelineRunRepository;
        this.videoRepository = videoRepository;
        this.objectMapper = objectMapper;
    }

    public PipelineRun execute(Video video) {
        PipelineRun run = new PipelineRun(video);
        run.setStatus("running");
        run = pipelineRunRepository.save(run);

        PipelineContext context = new PipelineContext();
        context.setVideo(video);
        Map<String, String> stageResults = new LinkedHashMap<>();

        video.setStatus("generating");
        videoRepository.save(video);

        log.info("Pipeline started for video {} (run {})", video.getId(), run.getId());

        for (PipelineStage stage : stages) {
            int attempts = 0;
            boolean success = false;

            while (attempts < maxAttempts && !success) {
                try {
                    run.setStage(stage.name());
                    pipelineRunRepository.save(run);

                    log.info("Executing stage: {} (attempt {})", stage.name(), attempts + 1);
                    long start = System.currentTimeMillis();

                    stage.execute(context);

                    long elapsed = System.currentTimeMillis() - start;
                    stageResults.put(stage.name(), "completed (" + elapsed + "ms)");
                    log.info("Stage {} completed in {}ms", stage.name(), elapsed);
                    success = true;

                } catch (Exception e) {
                    attempts++;
                    stageResults.put(stage.name(), "failed (attempt " + attempts + "): " + e.getMessage());
                    log.error("Stage {} failed (attempt {}): {}", stage.name(), attempts, e.getMessage());

                    if (attempts < maxAttempts) {
                        try {
                            Thread.sleep(backoffMs);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }

            if (!success) {
                run.setStatus("failed");
                run.setErrorMessage("Stage '" + stage.name() + "' failed after " + maxAttempts + " attempts");
                run.setCompletedAt(LocalDateTime.now());
                saveStageResults(run, stageResults);
                pipelineRunRepository.save(run);

                video.setStatus("failed");
                videoRepository.save(video);

                log.error("Pipeline failed at stage {}", stage.name());
                return run;
            }
        }

        run.setStatus("completed");
        run.setCompletedAt(LocalDateTime.now());
        saveStageResults(run, stageResults);
        pipelineRunRepository.save(run);

        video.setStatus("uploaded");
        videoRepository.save(video);

        log.info("Pipeline completed successfully for video {}", video.getId());
        return run;
    }

    private void saveStageResults(PipelineRun run, Map<String, String> stageResults) {
        try {
            run.setStagesJson(objectMapper.writeValueAsString(stageResults));
        } catch (Exception e) {
            log.warn("Failed to serialize stage results", e);
        }
    }
}
