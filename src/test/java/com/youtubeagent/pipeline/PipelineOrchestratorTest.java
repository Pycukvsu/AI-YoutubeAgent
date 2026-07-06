package com.youtubeagent.pipeline;

import com.youtubeagent.entity.Video;
import com.youtubeagent.repository.PipelineRunRepository;
import com.youtubeagent.repository.VideoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.exceptions.base.MockitoException;

@ExtendWith(MockitoExtension.class)
class PipelineOrchestratorTest {

    @Mock
    private PipelineRunRepository pipelineRunRepository;
    @Mock
    private VideoRepository videoRepository;
    @Mock
    private PipelineStage mockStage;

    private PipelineOrchestrator orchestrator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        when(mockStage.name()).thenReturn("test_stage");
        when(pipelineRunRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(videoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orchestrator = new PipelineOrchestrator(
                List.of(mockStage),
                pipelineRunRepository,
                videoRepository,
                objectMapper
        );
    }

    @Test
    void executesAllStagesOnSuccess() {
        Video video = new Video("Test", null);
        video.setId(1L);

        orchestrator.execute(video);

        verify(videoRepository, atLeastOnce()).save(video);
    }

    @Test
    void marksVideoAsFailedOnStageFailure() {
        lenient().doThrow(new RuntimeException("Stage failed")).when(mockStage).execute(any());

        Video video = new Video("Test", null);
        video.setId(1L);

        orchestrator.execute(video);

        verify(videoRepository, atLeast(2)).save(video);
    }
}
