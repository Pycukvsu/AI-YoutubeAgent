package com.youtubeagent.service;

import com.youtubeagent.config.FfmpegConfig;
import com.youtubeagent.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FfmpegService {

    private static final Logger log = LoggerFactory.getLogger(FfmpegService.class);

    private final FfmpegConfig config;
    private final ProcessExecutor processExecutor;

    public FfmpegService(FfmpegConfig config, ProcessExecutor processExecutor) {
        this.config = config;
        this.processExecutor = processExecutor;
    }

    public void imageToVideo(String imagePath, String outputPath, double durationSeconds) throws Exception {
        String fps = "30";
        int totalFrames = (int) (durationSeconds * 30);

        processExecutor.execute(
                config.getPath(), "-y",
                "-loop", "1",
                "-i", imagePath,
                "-vf", "scale=" + config.getWidth() + ":" + config.getHeight()
                        + ":force_original_aspect_ratio=decrease,pad="
                        + config.getWidth() + ":" + config.getHeight()
                        + ":(ow-iw)/2:(oh-ih)/2:black,"
                        + "zoompan=z='min(zoom+0.0015,1.5)':d=" + totalFrames
                        + ":x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s="
                        + config.getWidth() + "x" + config.getHeight()
                        + ",format=yuv420p",
                "-t", String.valueOf(durationSeconds),
                "-r", fps,
                "-c:v", config.getVideoCodec(),
                "-crf", String.valueOf(config.getCrf()),
                outputPath
        );
        log.info("Converted image to video: {} -> {} ({}s)", imagePath, outputPath, durationSeconds);
    }

    public String getAudioDuration(String audioPath) throws Exception {
        String output = processExecutor.execute(
                config.getPath(), "-i", audioPath, "-show_entries", "format=duration",
                "-v", "quiet", "-of", "csv=p=0"
        );
        return output.trim();
    }

    public void trimClip(String inputPath, String outputPath, double startTime, double duration) throws Exception {
        processExecutor.execute(
                config.getPath(), "-y", "-ss", String.valueOf(startTime),
                "-i", inputPath, "-t", String.valueOf(duration),
                "-c:v", config.getVideoCodec(), "-an",
                "-vf", "scale=" + config.getWidth() + ":" + config.getHeight() + ":force_original_aspect_ratio=decrease,pad="
                        + config.getWidth() + ":" + config.getHeight() + ":(ow-iw)/2:(oh-ih)/2",
                "-crf", String.valueOf(config.getCrf()),
                outputPath
        );
        log.info("Trimmed clip: {} -> {} (start={}, duration={})", inputPath, outputPath, startTime, duration);
    }

    public void concatenateClips(List<String> clipPaths, String outputPath) throws Exception {
        java.util.List<String> cmd = new java.util.ArrayList<>();
        cmd.add(config.getPath());
        cmd.add("-y");

        int targetWidth = config.getWidth();
        int targetHeight = config.getHeight();

        StringBuilder filterBuilder = new StringBuilder();
        java.util.List<String> scaledLabels = new java.util.ArrayList<>();

        for (int i = 0; i < clipPaths.size(); i++) {
            cmd.add("-i");
            cmd.add(clipPaths.get(i));
            String label = "s" + i;
            scaledLabels.add("[" + label + "]");
            filterBuilder.append("[").append(i).append(":v]scale=")
                    .append(targetWidth).append(":").append(targetHeight)
                    .append(":force_original_aspect_ratio=decrease,pad=")
                    .append(targetWidth).append(":").append(targetHeight)
                    .append(":(ow-iw)/2:(oh-ih)/2:black,format=yuv420p,setsar=1:1[").append(label).append("];");
        }

        for (String label : scaledLabels) {
            filterBuilder.append(label);
        }
        filterBuilder.append("concat=n=").append(clipPaths.size()).append(":v=1:a=0[outv]");

        cmd.add("-filter_complex");
        cmd.add(filterBuilder.toString());
        cmd.add("-map");
        cmd.add("[outv]");
        cmd.add("-c:v");
        cmd.add(config.getVideoCodec());
        cmd.add("-crf");
        cmd.add(String.valueOf(config.getCrf()));
        cmd.add(outputPath);

        processExecutor.execute(cmd.toArray(new String[0]));
        log.info("Concatenated {} clips -> {}", clipPaths.size(), outputPath);
    }

    public void addAudio(String videoPath, String audioPath, String outputPath) throws Exception {
        processExecutor.execute(
                config.getPath(), "-y",
                "-i", videoPath, "-i", audioPath,
                "-c:v", "copy", "-c:a", config.getAudioCodec(),
                "-map", "0:v:0", "-map", "1:a:0",
                "-shortest",
                outputPath
        );
        log.info("Added audio to video: {} -> {}", videoPath, outputPath);
    }

    public void burnSubtitles(String videoPath, String srtPath, String outputPath) throws Exception {
        String escapedSrt = srtPath.replace("'", "'\\\\''");
        processExecutor.execute(
                config.getPath(), "-y",
                "-i", videoPath,
                "-vf", "subtitles=" + escapedSrt + ":force_style='FontSize=16,PrimaryColour=&H00FFFFFF,OutlineColour=&H00000000,Outline=1,Shadow=0,Alignment=2,MarginV=30'",
                "-c:v", config.getVideoCodec(), "-crf", String.valueOf(config.getCrf()),
                "-c:a", "copy",
                outputPath
        );
        log.info("Burned subtitles: {} -> {}", videoPath, outputPath);
    }

    public void addBackgroundMusic(String videoPath, String musicPath, String outputPath, double volumeDb) throws Exception {
        processExecutor.execute(
                config.getPath(), "-y",
                "-i", videoPath, "-i", musicPath,
                "-filter_complex", "[1:a]volume=" + volumeDb + "dB[music];[0:a][music]amix=inputs=2:duration=first[aout]",
                "-map", "0:v", "-map", "[aout]",
                "-c:v", "copy", "-c:a", config.getAudioCodec(),
                "-shortest",
                outputPath
        );
        log.info("Added background music: {} -> {}", videoPath, outputPath);
    }

    public String assembleFull(String videoClipsDir, String audioPath, String srtPath,
                                String musicPath, String outputPath) throws Exception {
        Path tempDir = Paths.get(outputDir(outputPath));
        Files.createDirectories(tempDir);

        String concatPath = tempDir.resolve("concat.mp4").toString();
        String withAudioPath = tempDir.resolve("with_audio.mp4").toString();
        String withSubsPath = tempDir.resolve("with_subs.mp4").toString();

        List<String> clips = Files.list(Paths.get(videoClipsDir))
                .filter(p -> p.toString().endsWith(".mp4"))
                .sorted()
                .map(Path::toString)
                .toList();

        if (clips.isEmpty()) {
            throw new IllegalStateException("No video clips found in " + videoClipsDir);
        }

        concatenateClips(clips, concatPath);
        addAudio(concatPath, audioPath, withAudioPath);

        if (srtPath != null && Files.exists(Paths.get(srtPath))) {
            burnSubtitles(withAudioPath, srtPath, withSubsPath);
        } else {
            withSubsPath = withAudioPath;
        }

        if (musicPath != null && Files.exists(Paths.get(musicPath))) {
            addBackgroundMusic(withSubsPath, musicPath, outputPath, -20);
        } else {
            Files.move(Paths.get(withSubsPath), Paths.get(outputPath));
        }

        cleanupTemp(tempDir);
        log.info("Full video assembled: {}", outputPath);
        return outputPath;
    }

    private String outputDir(String outputPath) {
        return Paths.get(outputPath).getParent().toString();
    }

    private void cleanupTemp(Path tempDir) {
        try {
            if (Files.exists(tempDir)) {
                Files.list(tempDir)
                        .filter(p -> !p.getFileName().toString().equals("final.mp4"))
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                        });
            }
        } catch (Exception ignored) {}
    }
}
