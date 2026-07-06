package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pipeline")
public class PipelineConfig {

    private Retry retry = new Retry();
    private String tempDir = "/tmp/youtube-agent";
    private String musicDir = "classpath:music";
    private int maxConcurrent = 2;

    public Retry getRetry() { return retry; }
    public void setRetry(Retry retry) { this.retry = retry; }

    public String getTempDir() { return tempDir; }
    public void setTempDir(String tempDir) { this.tempDir = tempDir; }

    public String getMusicDir() { return musicDir; }
    public void setMusicDir(String musicDir) { this.musicDir = musicDir; }

    public int getMaxConcurrent() { return maxConcurrent; }
    public void setMaxConcurrent(int maxConcurrent) { this.maxConcurrent = maxConcurrent; }

    public static class Retry {
        private int maxAttempts = 3;
        private long backoffMs = 5000;

        public int getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }

        public long getBackoffMs() { return backoffMs; }
        public void setBackoffMs(long backoffMs) { this.backoffMs = backoffMs; }
    }
}
