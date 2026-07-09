package com.youtubeagent.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtubeagent.config.EdgeTtsConfig;
import com.youtubeagent.exception.ExternalServiceException;
import com.youtubeagent.util.ProcessExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GoogleTrendsService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTrendsService.class);

    private final EdgeTtsConfig ttsConfig;
    private final ProcessExecutor processExecutor;
    private final ObjectMapper objectMapper;

    public GoogleTrendsService(EdgeTtsConfig ttsConfig, ProcessExecutor processExecutor, ObjectMapper objectMapper) {
        this.ttsConfig = ttsConfig;
        this.processExecutor = processExecutor;
        this.objectMapper = objectMapper;
    }

    public String fetchTrends(String geo, int count) {
        try {
            Path scriptPath = extractScript();

            String[] command = {
                    ttsConfig.getPythonPath(),
                    scriptPath.toAbsolutePath().toString(),
                    geo,
                    String.valueOf(count)
            };

            log.info("Fetching Google Trends for geo={}, count={}", geo, count);
            String output = processExecutor.execute(30_000, command);

            JsonNode root = objectMapper.readTree(output);
            JsonNode topics = root.path("topics");

            StringBuilder result = new StringBuilder();
            for (JsonNode topic : topics) {
                result.append(topic.path("topic").asText()).append("\n");
            }

            log.info("Google Trends: {} topics found", topics.size());
            return result.toString();

        } catch (Exception e) {
            log.error("Google Trends fetch failed: {}", e.getMessage());
            throw new ExternalServiceException("GoogleTrends", e);
        }
    }

    private Path extractScript() throws Exception {
        ClassPathResource resource = new ClassPathResource("scripts/google_trends.py");
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "youtube-agent-trends");
        Files.createDirectories(tempDir);
        Path scriptPath = tempDir.resolve("google_trends.py");

        if (!Files.exists(scriptPath)) {
            Files.copy(resource.getInputStream(), scriptPath);
        }

        return scriptPath;
    }
}
