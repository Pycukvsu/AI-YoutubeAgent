package com.youtubeagent.controller;

import com.youtubeagent.service.SeoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seo")
public class SeoController {

    private final SeoService seoService;

    public SeoController(SeoService seoService) {
        this.seoService = seoService;
    }

    @PostMapping("/optimize")
    public ResponseEntity<SeoService.SeoResult> optimize(@RequestBody Map<String, String> request) {
        String topic = request.get("topic");
        String language = request.getOrDefault("language", "ru");
        return ResponseEntity.ok(seoService.optimize(topic, language));
    }

    @PostMapping("/suggest-titles")
    public ResponseEntity<List<String>> suggestTitles(@RequestBody Map<String, Object> request) {
        String topic = (String) request.get("topic");
        int count = (int) request.getOrDefault("count", 5);
        return ResponseEntity.ok(seoService.suggestTitles(topic, count));
    }
}
