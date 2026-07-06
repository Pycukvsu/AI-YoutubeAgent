package com.youtubeagent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trend")
public class Trend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String topic;

    @Column(nullable = false, length = 50)
    private String source;

    private Integer searchVolume;

    @Column(length = 100)
    private String category;

    @Column(columnDefinition = "jsonb")
    private String rawData;

    @Column(length = 20)
    private String status = "new";

    private LocalDateTime discoveredAt = LocalDateTime.now();
    private LocalDateTime usedAt;
    private LocalDateTime expiresAt;

    public Trend() {}

    public Trend(String topic, String source) {
        this.topic = topic;
        this.source = source;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Integer getSearchVolume() { return searchVolume; }
    public void setSearchVolume(Integer searchVolume) { this.searchVolume = searchVolume; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
