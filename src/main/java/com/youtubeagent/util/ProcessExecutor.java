package com.youtubeagent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.concurrent.TimeUnit;

@Component
public class ProcessExecutor {

    private static final Logger log = LoggerFactory.getLogger(ProcessExecutor.class);
    private static final long DEFAULT_TIMEOUT_MS = 120_000;

    public String execute(String... command) throws IOException, InterruptedException {
        return execute(DEFAULT_TIMEOUT_MS, command);
    }

    public String execute(long timeoutMs, String... command) throws IOException, InterruptedException {
        log.debug("Executing: {}", String.join(" ", command));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            output = sb.toString();
        }

        boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new InterruptedException("Process timed out after " + timeoutMs + "ms: " + String.join(" ", command));
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            log.error("Process exited with code {}: {}", exitCode, output);
            throw new IOException("Process failed with exit code " + exitCode + ": " + output);
        }

        log.debug("Process output: {}", output);
        return output;
    }
}
