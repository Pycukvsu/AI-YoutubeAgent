package com.youtubeagent.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SubtitleGenerator {

    public String generateSrt(String scriptText, double totalDurationSeconds) {
        String[] sentences = scriptText.split("(?<=[.!?])\\s+");
        List<String> segments = new ArrayList<>();

        StringBuilder current = new StringBuilder();
        for (String sentence : sentences) {
            if (current.length() + sentence.length() > 60) {
                if (!current.isEmpty()) {
                    segments.add(current.toString().trim());
                    current = new StringBuilder();
                }
            }
            current.append(sentence).append(" ");
        }
        if (!current.isEmpty()) {
            segments.add(current.toString().trim());
        }

        double segmentDuration = totalDurationSeconds / Math.max(segments.size(), 1);
        StringBuilder srt = new StringBuilder();

        for (int i = 0; i < segments.size(); i++) {
            double start = i * segmentDuration;
            double end = (i + 1) * segmentDuration;

            srt.append(i + 1).append("\n");
            srt.append(formatTime(start)).append(" --> ").append(formatTime(end)).append("\n");
            srt.append(segments.get(i)).append("\n\n");
        }

        return srt.toString();
    }

    private String formatTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        int millis = (int) ((seconds % 1) * 1000);

        return String.format("%02d:%02d:%02d,%03d", hours, minutes, secs, millis);
    }
}
