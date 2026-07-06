package com.youtubeagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ffmpeg")
public class FfmpegConfig {

    private String path = "ffmpeg";
    private String videoCodec = "libx264";
    private String audioCodec = "aac";
    private int width = 1080;
    private int height = 1920;
    private int crf = 23;

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getVideoCodec() { return videoCodec; }
    public void setVideoCodec(String videoCodec) { this.videoCodec = videoCodec; }

    public String getAudioCodec() { return audioCodec; }
    public void setAudioCodec(String audioCodec) { this.audioCodec = audioCodec; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int getCrf() { return crf; }
    public void setCrf(int crf) { this.crf = crf; }
}
