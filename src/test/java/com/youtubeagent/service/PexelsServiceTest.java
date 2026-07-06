package com.youtubeagent.service;

import com.youtubeagent.config.PexelsConfig;
import com.youtubeagent.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PexelsServiceTest {

    private PexelsService pexelsService;

    @BeforeEach
    void setUp() {
        PexelsConfig config = new PexelsConfig();
        config.setApiKey("test-key");
        config.setBaseUrl("https://api.pexels.com");
        config.setMinDurationSeconds(5);

        pexelsService = new PexelsService(config, new com.fasterxml.jackson.databind.ObjectMapper());
    }

    @Test
    void throwsOnInvalidApiKey() {
        assertThrows(ExternalServiceException.class,
                () -> pexelsService.searchVideos("nature", 4));
    }

    @Test
    void videoClipRecordHoldsData() {
        PexelsService.VideoClip clip = new PexelsService.VideoClip("123", "https://example.com/video.mp4", 10);

        assertEquals("123", clip.id());
        assertEquals("https://example.com/video.mp4", clip.downloadUrl());
        assertEquals(10, clip.durationSeconds());
    }

    @Test
    void throwsOnDownloadFailure() {
        PexelsService.VideoClip clip = new PexelsService.VideoClip("123", "https://invalid-url.example/video.mp4", 10);

        assertThrows(ExternalServiceException.class,
                () -> pexelsService.downloadClip(clip, "/tmp/pexels-test"));
    }
}
