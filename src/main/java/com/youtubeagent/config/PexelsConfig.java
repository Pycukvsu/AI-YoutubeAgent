package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pexels")
public class PexelsConfig {

    private String apiKey;
    private String baseUrl = "https://api.pexels.com";
    private int clipsPerVideo = 4;
    private int minDurationSeconds = 5;

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public int getClipsPerVideo() { return clipsPerVideo; }
    public void setClipsPerVideo(int clipsPerVideo) { this.clipsPerVideo = clipsPerVideo; }

    public int getMinDurationSeconds() { return minDurationSeconds; }
    public void setMinDurationSeconds(int minDurationSeconds) { this.minDurationSeconds = minDurationSeconds; }
}
