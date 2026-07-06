package com.youtubeagent.dto;

import jakarta.validation.constraints.NotBlank;

public class ScriptRequest {

    @NotBlank
    private String topic;

    private String tone = "informative";
    private String language = "ru";
    private Integer targetDurationSeconds = 45;

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getTone() { return tone; }
    public void setTone(String tone) { this.tone = tone; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Integer getTargetDurationSeconds() { return targetDurationSeconds; }
    public void setTargetDurationSeconds(Integer targetDurationSeconds) { this.targetDurationSeconds = targetDurationSeconds; }
}
