package com.youtubeagent.controller;

import com.youtubeagent.service.EnhancedAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class EnhancedAnalyticsController {

    private final EnhancedAnalyticsService analyticsService;

    public EnhancedAnalyticsController(EnhancedAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getChannelOverview() {
        return ResponseEntity.ok(analyticsService.getChannelOverview());
    }

    @GetMapping("/video/{videoId}")
    public ResponseEntity<Map<String, Object>> getVideoPerformance(@PathVariable Long videoId) {
        return ResponseEntity.ok(analyticsService.getVideoPerformance(videoId));
    }

    @GetMapping("/top")
    public ResponseEntity<List<Map<String, Object>>> getTopPerformers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getTopPerformers(limit));
    }

    @GetMapping("/best-times")
    public ResponseEntity<Map<String, Object>> getBestPostingTimes() {
        return ResponseEntity.ok(analyticsService.getBestPostingTimes());
    }

    @PostMapping("/collect")
    public ResponseEntity<String> collectAnalytics() {
        analyticsService.collectAnalytics();
        return ResponseEntity.ok("Analytics collection triggered");
    }
}
