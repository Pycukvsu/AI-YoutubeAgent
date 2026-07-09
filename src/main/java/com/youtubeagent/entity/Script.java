package com.youtubeagent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "script")
public class Script {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(nullable = false)
    private String content;

    private Integer wordCount;
    private Integer durationEstimateSeconds;

    @Column(length = 50)
    private String tone;

    @Column(length = 50)
    private String openaiModel;

    private Integer openaiTokensUsed;

    private LocalDateTime generatedAt = LocalDateTime.now();

    public Script() {}

    public Script(Video video, String content) {
        this.video = video;
        this.content = content;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

    public Integer getDurationEstimateSeconds() { return durationEstimateSeconds; }
    public void setDurationEstimateSeconds(Integer durationEstimateSeconds) { this.durationEstimateSeconds = durationEstimateSeconds; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public String getOpenaiModel() { return openaiModel; }
    public void setOpenaiModel(String openaiModel) { this.openaiModel = openaiModel; }

    public Integer getOpenaiTokensUsed() { return openaiTokensUsed; }
    public void setOpenaiTokensUsed(Integer openaiTokensUsed) { this.openaiTokensUsed = openaiTokensUsed; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
