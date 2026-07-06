package com.youtubeagent.pipeline;

public class PipelineStageException extends RuntimeException {

    private final String stageName;

    public PipelineStageException(String stageName, String message) {
        super("Stage '" + stageName + "' failed: " + message);
        this.stageName = stageName;
    }

    public PipelineStageException(String stageName, Throwable cause) {
        super("Stage '" + stageName + "' failed: " + cause.getMessage(), cause);
        this.stageName = stageName;
    }

    public String getStageName() {
        return stageName;
    }
}
