package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "trend-discovery")
public class TrendDiscoveryConfig {

    private int topicsPerRun = 10;
    private String language = "ru";
    private List<String> categories = List.of("technology", "science", "history", "psychology", "facts");
    private int minViralScore = 7;
    private boolean enableYoutubeSearch = true;
    private boolean enableOpenAiGeneration = true;

    public int getTopicsPerRun() { return topicsPerRun; }
    public void setTopicsPerRun(int topicsPerRun) { this.topicsPerRun = topicsPerRun; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public int getMinViralScore() { return minViralScore; }
    public void setMinViralScore(int minViralScore) { this.minViralScore = minViralScore; }

    public boolean isEnableYoutubeSearch() { return enableYoutubeSearch; }
    public void setEnableYoutubeSearch(boolean enableYoutubeSearch) { this.enableYoutubeSearch = enableYoutubeSearch; }

    public boolean isEnableOpenAiGeneration() { return enableOpenAiGeneration; }
    public void setEnableOpenAiGeneration(boolean enableOpenAiGeneration) { this.enableOpenAiGeneration = enableOpenAiGeneration; }
}
