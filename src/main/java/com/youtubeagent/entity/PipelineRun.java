package com.youtubeagent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pipeline_run")
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(length = 50)
    private String stage;

    @Column(length = 20)
    private String status = "running";

    @Column(columnDefinition = "text")
    private String errorMessage;

    private Integer retryCount = 0;

    private LocalDateTime startedAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    @Column(columnDefinition = "jsonb")
    private String stagesJson;

    public PipelineRun() {}

    public PipelineRun(Video video) {
        this.video = video;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getStagesJson() { return stagesJson; }
    public void setStagesJson(String stagesJson) { this.stagesJson = stagesJson; }
}
