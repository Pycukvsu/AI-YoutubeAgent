package com.youtubeagent.controller;

import com.youtubeagent.dto.PipelineRunDto;
import com.youtubeagent.dto.ScriptRequest;
import com.youtubeagent.entity.PipelineRun;
import com.youtubeagent.entity.Video;
import com.youtubeagent.pipeline.PipelineOrchestrator;
import com.youtubeagent.repository.PipelineRunRepository;
import com.youtubeagent.repository.VideoRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private final PipelineOrchestrator pipelineOrchestrator;
    private final VideoRepository videoRepository;
    private final PipelineRunRepository pipelineRunRepository;

    public PipelineController(PipelineOrchestrator pipelineOrchestrator,
                               VideoRepository videoRepository,
                               PipelineRunRepository pipelineRunRepository) {
        this.pipelineOrchestrator = pipelineOrchestrator;
        this.videoRepository = videoRepository;
        this.pipelineRunRepository = pipelineRunRepository;
    }

    @PostMapping("/start")
    public ResponseEntity<PipelineRunDto> startPipeline(@RequestBody(required = false) ScriptRequest request) {
        Video video = new Video("Pending...", null);
        video = videoRepository.save(video);

        final Video finalVideo = video;
        CompletableFuture.runAsync(() -> pipelineOrchestrator.execute(finalVideo));

        PipelineRunDto dto = new PipelineRunDto();
        dto.setVideoId(video.getId());
        dto.setStatus("started");
        return ResponseEntity.accepted().body(dto);
    }

    @PostMapping("/start/{videoId}")
    public ResponseEntity<PipelineRunDto> retryPipeline(@PathVariable Long videoId) {
        Video video = videoRepository.findById(videoId).orElse(null);
        if (video == null) {
            return ResponseEntity.notFound().build();
        }

        video.setStatus("pending");
        videoRepository.save(video);

        final Video finalVideo = video;
        CompletableFuture.runAsync(() -> pipelineOrchestrator.execute(finalVideo));

        PipelineRunDto dto = new PipelineRunDto();
        dto.setVideoId(videoId);
        dto.setStatus("retrying");
        return ResponseEntity.accepted().body(dto);
    }

    @GetMapping("/{runId}/status")
    public ResponseEntity<PipelineRunDto> getRunStatus(@PathVariable Long runId) {
        PipelineRun run = pipelineRunRepository.findById(runId).orElse(null);
        if (run == null) {
            return ResponseEntity.notFound().build();
        }

        PipelineRunDto dto = new PipelineRunDto();
        dto.setId(run.getId());
        dto.setVideoId(run.getVideo().getId());
        dto.setStage(run.getStage());
        dto.setStatus(run.getStatus());
        dto.setErrorMessage(run.getErrorMessage());
        dto.setRetryCount(run.getRetryCount());
        dto.setStartedAt(run.getStartedAt());
        dto.setCompletedAt(run.getCompletedAt());
        dto.setStagesJson(run.getStagesJson());
        return ResponseEntity.ok(dto);
    }
}
