package com.youtubeagent.service;

import com.youtubeagent.config.EdgeTtsConfig;
import com.youtubeagent.exception.ExternalServiceException;
import com.youtubeagent.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class EdgeTtsService {

    private static final Logger log = LoggerFactory.getLogger(EdgeTtsService.class);

    private final EdgeTtsConfig config;
    private final ProcessExecutor processExecutor;

    public EdgeTtsService(EdgeTtsConfig config, ProcessExecutor processExecutor) {
        this.config = config;
        this.processExecutor = processExecutor;
    }

    public String generateAudio(String text, String outputPath) {
        return generateAudio(text, config.getVoice(), config.getRate(), outputPath);
    }

    public String generateAudio(String text, String voice, String rate, String outputPath) {
        try {
            Path output = Paths.get(outputPath);
            Files.createDirectories(output.getParent());

            String[] command = {
                    "edge-tts",
                    "--voice", voice,
                    "--rate", rate,
                    "--text", text,
                    "--write-media", outputPath
            };

            log.info("Generating TTS audio with voice '{}', output: {}", voice, outputPath);
            processExecutor.execute(60_000, command);

            if (!Files.exists(output) || Files.size(output) == 0) {
                throw new IOException("TTS output file is empty or missing: " + outputPath);
            }

            log.info("TTS audio generated: {} ({} bytes)", outputPath, Files.size(output));
            return outputPath;

        } catch (Exception e) {
            log.error("TTS generation failed: {}", e.getMessage());
            throw new ExternalServiceException("EdgeTTS", e);
        }
    }
}
