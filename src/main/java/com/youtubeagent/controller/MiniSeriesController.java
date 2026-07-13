package com.youtubeagent.controller;

import com.youtubeagent.entity.MiniSeries;
import com.youtubeagent.service.MiniSeriesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/series")
public class MiniSeriesController {

    private final MiniSeriesService miniSeriesService;

    public MiniSeriesController(MiniSeriesService miniSeriesService) {
        this.miniSeriesService = miniSeriesService;
    }

    @PostMapping
    public ResponseEntity<MiniSeries> createSeries(@RequestBody Map<String, Object> request) {
        String topic = (String) request.get("topic");
        int episodes = (int) request.getOrDefault("episodes", 5);
        return ResponseEntity.ok(miniSeriesService.createSeries(topic, episodes));
    }

    @GetMapping
    public ResponseEntity<List<MiniSeries>> getAllSeries() {
        return ResponseEntity.ok(miniSeriesService.getAllSeries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MiniSeries> getSeries(@PathVariable Long id) {
        MiniSeries series = miniSeriesService.getSeries(id);
        if (series == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(series);
    }

    @GetMapping("/{id}/next-episode")
    public ResponseEntity<Map<String, Object>> getNextEpisode(@PathVariable Long id) {
        Map<String, Object> episode = miniSeriesService.getNextEpisode(id);
        if (episode == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(episode);
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<String> generateEpisode(@PathVariable Long id) {
        miniSeriesService.generateEpisodeVideo(id);
        return ResponseEntity.ok("Episode generation started");
    }
}
