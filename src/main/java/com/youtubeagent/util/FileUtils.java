package com.youtubeagent.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static String createTempDir(String prefix) throws IOException {
        Path tempDir = Files.createTempDirectory(prefix);
        return tempDir.toAbsolutePath().toString();
    }

    public static void ensureDir(String path) throws IOException {
        Files.createDirectories(Path.of(path));
    }

    public static void deleteRecursively(String path) throws IOException {
        Path dir = Path.of(path);
        if (Files.exists(dir)) {
            Files.walk(dir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }
}
