package com.youtubeagent.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics_snapshot")
public class AnalyticsSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(length = 50)
    private String youtubeVideoId;

    private Long views = 0L;
    private Long likes = 0L;
    private Integer comments = 0;
    private Integer subscribersGained = 0;
    private BigDecimal watchTimeMinutes = BigDecimal.ZERO;

    private LocalDateTime snapshotAt = LocalDateTime.now();

    public AnalyticsSnapshot() {}

    public AnalyticsSnapshot(Video video, String youtubeVideoId) {
        this.video = video;
        this.youtubeVideoId = youtubeVideoId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

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
