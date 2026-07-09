package com.youtubeagent.controller;

import com.youtubeagent.entity.AbTest;
import com.youtubeagent.service.AbTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ab-tests")
public class AbTestController {

    private final AbTestService abTestService;

    public AbTestController(AbTestService abTestService) {
        this.abTestService = abTestService;
    }

    @PostMapping("/title")
    public ResponseEntity<AbTest> createTitleTest(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<String> variants = (List<String>) request.get("variants");
        return ResponseEntity.ok(abTestService.createTitleTest(name, variants));
    }

    @PostMapping("/thumbnail")
    public ResponseEntity<AbTest> createThumbnailTest(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<String> variants = (List<String>) request.get("variants");
        return ResponseEntity.ok(abTestService.createThumbnailTest(name, variants));
    }

    @PostMapping("/timing")
    public ResponseEntity<AbTest> createTimeTest(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        List<String> variants = (List<String>) request.get("variants");
        return ResponseEntity.ok(abTestService.createTimeTest(name, variants));
    }

    @GetMapping("/active")
    public ResponseEntity<List<AbTest>> getActiveTests() {
        return ResponseEntity.ok(abTestService.getActiveTests());
    }

    @GetMapping("/{testId}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeTest(@PathVariable Long testId) {
        return ResponseEntity.ok(abTestService.analyzeTest(testId));
    }

    @PostMapping("/{testId}/complete")
    public ResponseEntity<String> completeTest(@PathVariable Long testId) {
        abTestService.completeTest(testId);
        return ResponseEntity.ok("Test completed");
    }
}
