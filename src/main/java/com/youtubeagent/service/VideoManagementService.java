package com.youtubeagent.service;

import com.youtubeagent.dto.VideoDto;
import com.youtubeagent.entity.Video;
import com.youtubeagent.repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class VideoManagementService {

    private final VideoRepository videoRepository;

    public VideoManagementService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public Page<VideoDto> listVideos(int page, int size) {
        return videoRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(this::toDto);
    }

    public VideoDto getVideo(Long id) {
        return videoRepository.findById(id).map(this::toDto).orElse(null);
    }

    public VideoDto toDto(Video video) {
        VideoDto dto = new VideoDto();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setTags(video.getTags());
        dto.setStatus(video.getStatus());
        dto.setYoutubeId(video.getYoutubeId());
        dto.setYoutubeUrl(video.getYoutubeUrl());
        dto.setDurationSeconds(video.getDurationSeconds());
        dto.setCreatedAt(video.getCreatedAt());
        dto.setUploadedAt(video.getUploadedAt());

        if (video.getTrend() != null) {
            dto.setTrendTopic(video.getTrend().getTopic());
        }

        return dto;
    }
}
