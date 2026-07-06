package com.youtubeagent.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtitleGeneratorTest {

    private final SubtitleGenerator generator = new SubtitleGenerator();

    @Test
    void generatesValidSrtFormat() {
        String srt = generator.generateSrt("Привет мир. Это тест. Третий.", 9.0);

        assertTrue(srt.contains("00:00:00,000 --> 00:00:09,000"));
        assertTrue(srt.contains("Привет мир."));
        assertTrue(srt.contains("1\n"));
    }

    @Test
    void handlesSingleSentence() {
        String srt = generator.generateSrt("Одно предложение.", 5.0);

        assertTrue(srt.contains("00:00:00,000 --> 00:00:05,000"));
        assertTrue(srt.contains("Одно предложение."));
    }

    @Test
    void splitsLongTextIntoMultipleSegments() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            longText.append("Это предложение номер ").append(i).append(" для проверки. ");
        }
        String srt = generator.generateSrt(longText.toString(), 30.0);

        String[] blocks = srt.trim().split("\n\n");
        assertTrue(blocks.length > 1, "Long text should be split into multiple segments");
    }
}
