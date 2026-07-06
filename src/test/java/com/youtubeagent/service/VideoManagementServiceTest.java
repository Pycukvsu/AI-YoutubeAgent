package com.youtubeagent.service;

import com.youtubeagent.dto.VideoDto;
import com.youtubeagent.entity.Trend;
import com.youtubeagent.entity.Video;
import com.youtubeagent.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoManagementServiceTest {

    @Mock
    private VideoRepository videoRepository;

    private VideoManagementService videoManagementService;

    @BeforeEach
    void setUp() {
        videoManagementService = new VideoManagementService(videoRepository);
    }

    @Test
    void listVideosReturnsMappedDtos() {
        Trend trend = new Trend("AI news", "manual");
        Video video = new Video("Test Video", trend);
        video.setId(1L);
        video.setStatus("uploaded");
        video.setYoutubeId("abc123");

        when(videoRepository.findAllByOrderByCreatedAtDesc(any()))
                .thenReturn(new PageImpl<>(List.of(video)));

        Page<VideoDto> result = videoManagementService.listVideos(0, 20);

        assertEquals(1, result.getContent().size());
        assertEquals("Test Video", result.getContent().get(0).getTitle());
        assertEquals("uploaded", result.getContent().get(0).getStatus());
        assertEquals("AI news", result.getContent().get(0).getTrendTopic());
    }

    @Test
    void getVideoReturnsDtoWhenFound() {
        Video video = new Video("Test", null);
        video.setId(1L);

        when(videoRepository.findById(1L)).thenReturn(Optional.of(video));

        VideoDto dto = videoManagementService.getVideo(1L);

        assertNotNull(dto);
        assertEquals("Test", dto.getTitle());
    }

    @Test
    void getVideoReturnsNullWhenNotFound() {
        when(videoRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(videoManagementService.getVideo(99L));
    }

    @Test
    void toDtoMapsAllFields() {
        Trend trend = new Trend("Tech", "manual");
        Video video = new Video("My Video", trend);
        video.setId(10L);
        video.setDescription("A description");
        video.setTags(new String[]{"tag1", "tag2"});
        video.setStatus("generating");
        video.setYoutubeId("yt123");
        video.setYoutubeUrl("https://youtube.com/shorts/yt123");
        video.setDurationSeconds(45);

        VideoDto dto = videoManagementService.toDto(video);

        assertEquals(10L, dto.getId());
        assertEquals("My Video", dto.getTitle());
        assertEquals("A description", dto.getDescription());
        assertArrayEquals(new String[]{"tag1", "tag2"}, dto.getTags());
        assertEquals("generating", dto.getStatus());
        assertEquals("yt123", dto.getYoutubeId());
        assertEquals("https://youtube.com/shorts/yt123", dto.getYoutubeUrl());
        assertEquals(45, dto.getDurationSeconds());
        assertEquals("Tech", dto.getTrendTopic());
    }

    @Test
    void toDtoHandlesNullTrend() {
        Video video = new Video("No Trend", null);
        VideoDto dto = videoManagementService.toDto(video);

        assertNull(dto.getTrendTopic());
    }
}
