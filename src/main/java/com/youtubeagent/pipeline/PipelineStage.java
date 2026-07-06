package com.youtubeagent.pipeline;

public interface PipelineStage {

    String name();

    void execute(PipelineContext context) throws PipelineStageException;

    default void rollback(PipelineContext context) {
        // optional cleanup on failure
    }
}
