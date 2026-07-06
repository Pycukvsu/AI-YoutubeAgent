package com.youtubeagent.controller;

import com.youtubeagent.dto.AnalyticsDto;
import com.youtubeagent.entity.Trend;
import com.youtubeagent.dto.TrendDto;
import com.youtubeagent.service.AnalyticsService;
import com.youtubeagent.service.TrendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final TrendService trendService;

    public AnalyticsController(AnalyticsService analyticsService, TrendService trendService) {
        this.analyticsService = analyticsService;
        this.trendService = trendService;
    }

    @GetMapping("/analytics/video/{videoId}")
    public ResponseEntity<List<AnalyticsDto>> getVideoAnalytics(@PathVariable Long videoId) {
        return ResponseEntity.ok(analyticsService.getVideoAnalytics(videoId));
    }

    @PostMapping("/analytics/refresh")
    public ResponseEntity<String> refreshAnalytics() {
        analyticsService.refreshAllAnalytics();
        return ResponseEntity.ok("Analytics refresh triggered");
    }

    @GetMapping("/trends")
    public ResponseEntity<List<TrendDto>> listTrends() {
        List<TrendDto> trends = trendService.getAvailableTrends().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(trends);
    }

    @PostMapping("/trends")
    public ResponseEntity<TrendDto> addTrend(@RequestBody TrendDto request) {
        Trend trend = trendService.addTrend(request.getTopic(), request.getSource());
        return ResponseEntity.ok(toDto(trend));
    }

    private TrendDto toDto(Trend trend) {
        TrendDto dto = new TrendDto();
        dto.setId(trend.getId());
        dto.setTopic(trend.getTopic());
        dto.setSource(trend.getSource());
        dto.setSearchVolume(trend.getSearchVolume());
        dto.setCategory(trend.getCategory());
        dto.setStatus(trend.getStatus());
        dto.setDiscoveredAt(trend.getDiscoveredAt());
        return dto;
    }
}
