package com.youtubeagent.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_analytics")
public class VideoAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    private Long views = 0L;
    private Long likes = 0L;
    private Integer comments = 0;
    private Integer shares = 0;
    private Integer subscribersGained = 0;
    private BigDecimal watchTimeMinutes = BigDecimal.ZERO;
    private BigDecimal avgViewDurationSeconds = BigDecimal.ZERO;
    private BigDecimal clickThroughRate = BigDecimal.ZERO;

    @Column(length = 50)
    private String variant;

    private LocalDateTime recordedAt = LocalDateTime.now();

    public VideoAnalytics() {}

    public VideoAnalytics(Video video) {
        this.video = video;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Video getVideo() { return video; }
    public void setVideo(Video video) { this.video = video; }

    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }

    public Long getLikes() { return likes; }
    public void setLikes(Long likes) { this.likes = likes; }

    public Integer getComments() { return comments; }
    public void setComments(Integer comments) { this.comments = comments; }

    public Integer getShares() { return shares; }
    public void setShares(Integer shares) { this.shares = shares; }

    public Integer getSubscribersGained() { return subscribersGained; }
    public void setSubscribersGained(Integer subscribersGained) { this.subscribersGained = subscribersGained; }

    public BigDecimal getWatchTimeMinutes() { return watchTimeMinutes; }
    public void setWatchTimeMinutes(BigDecimal watchTimeMinutes) { this.watchTimeMinutes = watchTimeMinutes; }

    public BigDecimal getAvgViewDurationSeconds() { return avgViewDurationSeconds; }
    public void setAvgViewDurationSeconds(BigDecimal avgViewDurationSeconds) { this.avgViewDurationSeconds = avgViewDurationSeconds; }

    public BigDecimal getClickThroughRate() { return clickThroughRate; }
    public void setClickThroughRate(BigDecimal clickThroughRate) { this.clickThroughRate = clickThroughRate; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
