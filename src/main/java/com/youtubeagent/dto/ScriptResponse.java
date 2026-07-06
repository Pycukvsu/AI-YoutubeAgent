package com.youtubeagent.dto;

public class ScriptResponse {

    private Long videoId;
    private String scriptText;
    private String title;
    private String description;
    private String[] tags;
    private Integer durationEstimateSeconds;

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getScriptText() { return scriptText; }
    public void setScriptText(String scriptText) { this.scriptText = scriptText; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public Integer getDurationEstimateSeconds() { return durationEstimateSeconds; }
    public void setDurationEstimateSeconds(Integer durationEstimateSeconds) { this.durationEstimateSeconds = durationEstimateSeconds; }
}
