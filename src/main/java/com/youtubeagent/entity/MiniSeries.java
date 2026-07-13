package com.youtubeagent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mini_series")
public class MiniSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String concept;

    @Column(nullable = false)
    private int totalEpisodes = 5;

    @Column(nullable = false)
    private int currentEpisode = 0;

    @Column(length = 50)
    private String status = "created";

    @Column(columnDefinition = "text")
    private String episodesJson;

    @Column(length = 100)
    private String category;

    @Column(length = 50)
    private String language = "ru";

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    public MiniSeries() {}

    public MiniSeries(String title, String concept, int totalEpisodes) {
        this.title = title;
        this.concept = concept;
        this.totalEpisodes = totalEpisodes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getConcept() { return concept; }
    public void setConcept(String concept) { this.concept = concept; }

    public int getTotalEpisodes() { return totalEpisodes; }
    public void setTotalEpisodes(int totalEpisodes) { this.totalEpisodes = totalEpisodes; }

    public int getCurrentEpisode() { return currentEpisode; }
    public void setCurrentEpisode(int currentEpisode) { this.currentEpisode = currentEpisode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEpisodesJson() { return episodesJson; }
    public void setEpisodesJson(String episodesJson) { this.episodesJson = episodesJson; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
