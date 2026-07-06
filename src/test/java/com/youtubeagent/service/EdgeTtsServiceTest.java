package com.youtubeagent.service;

import com.youtubeagent.config.EdgeTtsConfig;
import com.youtubeagent.exception.ExternalServiceException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdgeTtsServiceTest {

    @Mock
    private ProcessExecutor processExecutor;

    private EdgeTtsService edgeTtsService;
    private EdgeTtsConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        config = new EdgeTtsConfig();
        config.setPythonPath("python3");
        config.setWrapperScript("scripts/edge_tts_wrapper.py");
        config.setVoice("ru-RU-DmitryNeural");
        config.setRate("+0%");

        edgeTtsService = new EdgeTtsService(config, processExecutor);
    }

    @Test
    void throwsOnProcessFailure() throws Exception {
        when(processExecutor.execute(anyLong(), any(String[].class)))
                .thenThrow(new RuntimeException("Process failed"));

        assertThrows(ExternalServiceException.class,
                () -> edgeTtsService.generateAudio("test", "/tmp/out.mp3"));
    }

    @Test
    void throwsOnMissingOutputFile() throws Exception {
        when(processExecutor.execute(anyLong(), any(String[].class))).thenReturn("");

        assertThrows(ExternalServiceException.class,
                () -> edgeTtsService.generateAudio("test", tempDir.resolve("nonexistent.mp3").toString()));
    }

    @Test
    void delegatesToConfigDefaults() throws Exception {
        when(processExecutor.execute(anyLong(), any(String[].class))).thenReturn("");

        String outputPath = tempDir.resolve("output.mp3").toString();
        Files.writeString(Path.of(outputPath), "fake audio data");

        edgeTtsService.generateAudio("Привет мир", outputPath);

        verify(processExecutor).execute(eq(60_000L), any(String[].class));
    }
}
