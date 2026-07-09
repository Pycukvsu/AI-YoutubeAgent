package com.youtubeagent.service;

import com.youtubeagent.util.ProcessExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoQualityServiceTest {

    @Mock
    private ProcessExecutor processExecutor;

    private VideoQualityService videoQualityService;

    @BeforeEach
    void setUp() {
        videoQualityService = new VideoQualityService(processExecutor);
    }

    @Test
    void returnsErrorWhenFileNotFound() {
        VideoQualityService.QualityResult result = videoQualityService.checkVideo("/nonexistent/video.mp4");

        assertFalse(result.valid());
        assertTrue(result.message().contains("not found"));
    }

    @Test
    void returnsErrorOnProbeFailure(@TempDir Path tempDir) throws Exception {
        Path videoFile = tempDir.resolve("test.mp4");
        Files.writeString(videoFile, "fake video data");

        when(processExecutor.execute(any(String[].class)))
                .thenThrow(new RuntimeException("ffprobe not found"));

        VideoQualityService.QualityResult result = videoQualityService.checkVideo(videoFile.toString());

        assertFalse(result.valid());
        assertTrue(result.message().contains("Check failed"));
    }

    @Test
    void passesValidVideo(@TempDir Path tempDir) throws Exception {
        Path videoFile = tempDir.resolve("test.mp4");
        Files.writeString(videoFile, "fake video data");

        String ffprobeJson = """
                {
                    "format": {"duration": "45.0"},
                    "streams": [{"codec_type": "video", "width": 1080, "height": 1920, "codec_name": "h264"}]
                }
                """;
        when(processExecutor.execute(any(String[].class))).thenReturn(ffprobeJson);

        VideoQualityService.QualityResult result = videoQualityService.checkVideo(videoFile.toString());

        assertTrue(result.valid());
        assertEquals(45.0, result.durationSeconds(), 0.1);
        assertEquals(1080, result.width());
        assertEquals(1920, result.height());
    }

    @Test
    void failsOnShortVideo(@TempDir Path tempDir) throws Exception {
        Path videoFile = tempDir.resolve("short.mp4");
        Files.writeString(videoFile, "fake");

        String ffprobeJson = """
                {
                    "format": {"duration": "5.0"},
                    "streams": [{"codec_type": "video", "width": 1080, "height": 1920, "codec_name": "h264"}]
                }
                """;
        when(processExecutor.execute(any(String[].class))).thenReturn(ffprobeJson);

        VideoQualityService.QualityResult result = videoQualityService.checkVideo(videoFile.toString());

        assertFalse(result.valid());
        assertTrue(result.message().contains("Too short"));
    }

    @Test
    void failsOnLowResolution(@TempDir Path tempDir) throws Exception {
        Path videoFile = tempDir.resolve("lowres.mp4");
        Files.writeString(videoFile, "fake");

        String ffprobeJson = """
                {
                    "format": {"duration": "45.0"},
                    "streams": [{"codec_type": "video", "width": 480, "height": 854, "codec_name": "h264"}]
                }
                """;
        when(processExecutor.execute(any(String[].class))).thenReturn(ffprobeJson);

        VideoQualityService.QualityResult result = videoQualityService.checkVideo(videoFile.toString());

        assertFalse(result.valid());
        assertTrue(result.message().contains("Low resolution"));
    }

    @Test
    void failsOnHorizontalVideo(@TempDir Path tempDir) throws Exception {
        Path videoFile = tempDir.resolve("horizontal.mp4");
        Files.writeString(videoFile, "fake");

        String ffprobeJson = """
                {
                    "format": {"duration": "45.0"},
                    "streams": [{"codec_type": "video", "width": 1920, "height": 1080, "codec_name": "h264"}]
                }
                """;
        when(processExecutor.execute(any(String[].class))).thenReturn(ffprobeJson);

        VideoQualityService.QualityResult result = videoQualityService.checkVideo(videoFile.toString());

        assertFalse(result.valid());
        assertTrue(result.message().contains("Not vertical"));
    }
}
