package com.youtubeagent.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AnalyticsDto {

    private Long videoId;
    private String youtubeVideoId;
    private Long views;
    private Long likes;
    private Integer comments;
    private Integer subscribersGained;
    private BigDecimal watchTimeMinutes;
    private LocalDateTime snapshotAt;

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }

    public String getYoutubeVideoId() { return youtubeVideoId; }
    public void setYoutubeVideoId(String youtubeVideoId) { this.youtubeVideoId = youtubeVideoId; }

    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }

    public Long getLikes() { return likes; }
    public void setLikes(Long likes) { this.likes = likes; }

    public Integer getComments() { return comments; }
    public void setComments(Integer comments) { this.comments = comments; }

    public Integer getSubscribersGained() { return subscribersGained; }
    public void setSubscribersGained(Integer subscribersGained) { this.subscribersGained = subscribersGained; }

    public BigDecimal getWatchTimeMinutes() { return watchTimeMinutes; }
    public void setWatchTimeMinutes(BigDecimal watchTimeMinutes) { this.watchTimeMinutes = watchTimeMinutes; }

    public LocalDateTime getSnapshotAt() { return snapshotAt; }
    public void setSnapshotAt(LocalDateTime snapshotAt) { this.snapshotAt = snapshotAt; }
}
