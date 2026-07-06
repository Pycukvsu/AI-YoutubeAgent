package com.youtubeagent.service;

import com.youtubeagent.config.YoutubeConfig;
import com.youtubeagent.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YoutubeUploadServiceTest {

    private YoutubeUploadService youtubeService;

    @BeforeEach
    void setUp() {
        YoutubeConfig config = new YoutubeConfig();
        config.setClientId("test-client-id");
        config.setClientSecret("test-client-secret");
        config.setRefreshToken("test-refresh-token");
        config.setChannelId("test-channel-id");
        config.setDefaultCategory("22");
        config.setDefaultVisibility("PRIVATE");

        youtubeService = new YoutubeUploadService(config, new com.fasterxml.jackson.databind.ObjectMapper());
    }

    @Test
    void getAccessTokenFailsWithInvalidCredentials() {
        assertThrows(ExternalServiceException.class,
                () -> youtubeService.getAccessToken());
    }

    @Test
    void uploadFailsWithInvalidCredentials() {
        assertThrows(ExternalServiceException.class,
                () -> youtubeService.upload("/tmp/video.mp4", "Test", "Description", new String[]{"tag1"}));
    }

    @Test
    void getStatsReturnsZerosOnFailure() {
        YoutubeUploadService.VideoStats stats = youtubeService.getStats("invalid-id");
        assertEquals(0, stats.views());
        assertEquals(0, stats.likes());
        assertEquals(0, stats.comments());
    }

    @Test
    void uploadResultRecordWorks() {
        YoutubeUploadService.UploadResult result = new YoutubeUploadService.UploadResult("abc123", "https://youtube.com/shorts/abc123");
        assertEquals("abc123", result.videoId());
        assertEquals("https://youtube.com/shorts/abc123", result.url());
    }
}
