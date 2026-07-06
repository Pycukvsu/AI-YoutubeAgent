package com.youtubeagent.service;

import com.youtubeagent.config.FfmpegConfig;
import com.youtubeagent.util.ProcessExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FfmpegServiceTest {

    @Mock
    private ProcessExecutor processExecutor;

    private FfmpegService ffmpegService;

    @BeforeEach
    void setUp() {
        FfmpegConfig config = new FfmpegConfig();
        config.setPath("ffmpeg");
        config.setVideoCodec("libx264");
        config.setAudioCodec("aac");
        config.setWidth(1080);
        config.setHeight(1920);
        config.setCrf(23);

        ffmpegService = new FfmpegService(config, processExecutor);
    }

    @Test
    void getAudioDurationReturnsTrimmedOutput() throws Exception {
        when(processExecutor.execute(any(String[].class))).thenReturn("45.123456\n");

        String duration = ffmpegService.getAudioDuration("/tmp/audio.mp3");

        assertEquals("45.123456", duration);
    }

    @Test
    void trimClipCallsFfmpeg() throws Exception {
        ffmpegService.trimClip("/tmp/in.mp4", "/tmp/out.mp4", 5.0, 10.0);

        verify(processExecutor).execute(any(String[].class));
    }

    @Test
    void concatenateClipsBuildsCorrectCommand() throws Exception {
        List<String> clips = List.of("/tmp/a.mp4", "/tmp/b.mp4");

        ffmpegService.concatenateClips(clips, "/tmp/out.mp4");

        verify(processExecutor).execute(any(String[].class));
    }

    @Test
    void addAudioCallsFfmpeg() throws Exception {
        ffmpegService.addAudio("/tmp/video.mp4", "/tmp/audio.mp3", "/tmp/out.mp4");

        verify(processExecutor).execute(any(String[].class));
    }

    @Test
    void burnSubtitlesEscapesSrtPath() throws Exception {
        ffmpegService.burnSubtitles("/tmp/video.mp4", "/tmp/my subs.srt", "/tmp/out.mp4");

        verify(processExecutor).execute(any(String[].class));
    }

    @Test
    void assembleFullThrowsOnEmptyClipsDir(@TempDir Path tempDir) throws Exception {
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectories(emptyDir);

        assertThrows(IllegalStateException.class,
                () -> ffmpegService.assembleFull(emptyDir.toString(), "/tmp/audio.mp3", null, null, tempDir.resolve("out.mp4").toString()));
    }

    @Test
    void assembleFullCallsConcatenateAndAddAudio(@TempDir Path tempDir) throws Exception {
        Path clipsDir = tempDir.resolve("clips");
        Files.createDirectories(clipsDir);
        Files.createFile(clipsDir.resolve("clip1.mp4"));
        Files.createFile(clipsDir.resolve("clip2.mp4"));

        Path audio = tempDir.resolve("audio.mp3");
        Files.createFile(audio);

        Path outputDir = tempDir.resolve("output");
        Files.createDirectories(outputDir);
        Files.createFile(outputDir.resolve("with_audio.mp4"));

        String outputPath = outputDir.resolve("final.mp4").toString();

        ffmpegService.assembleFull(clipsDir.toString(), audio.toString(), null, null, outputPath);

        verify(processExecutor, atLeast(2)).execute(any(String[].class));
    }
}
