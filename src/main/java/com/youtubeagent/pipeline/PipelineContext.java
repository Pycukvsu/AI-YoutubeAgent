package com.youtubeagent.pipeline;

import com.youtubeagent.entity.Trend;
import com.youtubeagent.entity.Video;
import com.youtubeagent.entity.Script;
import java.util.HashMap;
import java.util.Map;

public class PipelineContext {

    private Video video;
    private Trend trend;
    private Script script;
    private String audioFilePath;
    private java.util.List<String> videoClips = new java.util.ArrayList<>();
    private String subtitleFile;
    private String finalVideoPath;
    private String youtubeVideoId;
    private String thumbnailPath;
    private final Map<String, Object> metadata = new HashMap<>();

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public Trend getTrend() { return trend; }
    public void setTrend(Trend trend) { this.trend = trend; }

    public Script getScript() { return script; }
    public void setScript(Script script) { this.script = script; }

    public String getAudioFilePath() { return audioFilePath; }
    public void setAudioFilePath(String audioFilePath) { this.audioFilePath = audioFilePath; }

    public java.util.List<String> getVideoClips() { return videoClips; }
    public void setVideoClips(java.util.List<String> videoClips) { this.videoClips = videoClips; }

    public String getSubtitleFile() { return subtitleFile; }
    public void setSubtitleFile(String subtitleFile) { this.subtitleFile = subtitleFile; }

    public String getFinalVideoPath() { return finalVideoPath; }
    public void setFinalVideoPath(String finalVideoPath) { this.finalVideoPath = finalVideoPath; }

    public String getYoutubeVideoId() { return youtubeVideoId; }
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = youtubeVideoId; }

    public String getThumbnailPath() { return thumbnailPath; }
    public void setThumbnailPath(String thumbnailPath) { this.thumbnailPath = thumbnailPath; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(String key, Object value) { this.metadata.put(key, value); }
}
