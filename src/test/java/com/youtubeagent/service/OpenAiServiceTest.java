package com.youtubeagent.service;

import com.youtubeagent.config.OpenAiConfig;
import com.youtubeagent.dto.ScriptRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OpenAiServiceTest {

    private OpenAiService openAiService;

    @BeforeEach
    void setUp() {
        OpenAiConfig config = new OpenAiConfig();
        config.setApiKey("test-key");
        config.setModel("gpt-4o");
        config.setBaseUrl("https://api.openai.com/v1");
        config.setMaxTokens(500);

        openAiService = new OpenAiService(config, new ObjectMapper());
    }

    @Test
    void throwsOnInvalidApiKey() {
        ScriptRequest request = new ScriptRequest();
        request.setTopic("test");

        assertThrows(Exception.class, () -> openAiService.generateScript(request));
    }
}
