package com.youtubeagent.service;

import com.youtubeagent.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class VideoQualityService {

    private static final Logger log = LoggerFactory.getLogger(VideoQualityService.class);

    private final ProcessExecutor processExecutor;

    public VideoQualityService(ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    public QualityResult checkVideo(String videoPath) {
        try {
            Path path = Path.of(videoPath);
            if (!Files.exists(path)) {
                return new QualityResult(false, "File not found: " + videoPath, 0, 0, 0, 0);
            }

            long fileSizeMB = Files.size(path) / (1024 * 1024);
            if (fileSizeMB > 100) {
                return new QualityResult(false, "File too large: " + fileSizeMB + "MB (max 100MB)", 0, 0, 0, 0);
            }

            String ffprobeOutput = processExecutor.execute(
                    "ffprobe", "-v", "quiet", "-print_format", "json",
                    "-show_format", "-show_streams", videoPath
            );

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(ffprobeOutput);

            double duration = root.path("format").path("duration").asDouble(0);
            int width = 0;
            int height = 0;
            String codec = "";

            for (com.fasterxml.jackson.databind.JsonNode stream : root.path("streams")) {
                if ("video".equals(stream.path("codec_type").asText())) {
                    width = stream.path("width").asInt(0);
                    height = stream.path("height").asInt(0);
                    codec = stream.path("codec_name").asText("");
                }
            }

            StringBuilder errors = new StringBuilder();

            if (duration < 15) {
                errors.append("Too short: ").append(String.format("%.1f", duration)).append("s (min 15s). ");
            }
            if (duration > 180) {
                errors.append("Too long: ").append(String.format("%.1f", duration)).append("s (max 180s). ");
            }
            if (height < 1080) {
                errors.append("Low resolution: ").append(width).append("x").append(height).append(" (min 1080p). ");
            }
            if (width > height) {
                errors.append("Not vertical: ").append(width).append("x").append(height).append(". ");
            }
            if (!"h264".equals(codec) && !".mp4".endsWith(videoPath)) {
                errors.append("Codec not h264: ").append(codec).append(". ");
            }

            boolean valid = errors.isEmpty();
            String message = valid ? "Video OK" : errors.toString().trim();

            log.info("Video quality check: {} - {}", valid ? "PASS" : "FAIL", message);

            return new QualityResult(valid, message, duration, width, height, fileSizeMB);

        } catch (Exception e) {
            log.error("Video quality check failed: {}", e.getMessage());
            return new QualityResult(false, "Check failed: " + e.getMessage(), 0, 0, 0, 0);
        }
    }

    public record QualityResult(boolean valid, String message, double durationSeconds,
                                 int width, int height, long fileSizeMB) {}
}
