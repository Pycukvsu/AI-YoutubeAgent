package com.youtubeagent.dto;

import java.time.LocalDateTime;

public class TrendDto {

    private Long id;
    private String topic;
    private String source;
    private Integer searchVolume;
    private String category;
    private String status;
    private LocalDateTime discoveredAt;

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }
}
