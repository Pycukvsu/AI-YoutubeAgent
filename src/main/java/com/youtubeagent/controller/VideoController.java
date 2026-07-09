package com.youtubeagent.controller;

import com.youtubeagent.dto.VideoDto;
import com.youtubeagent.service.VideoManagementService;
import com.youtubeagent.service.YoutubeUploadService;
import com.youtubeagent.entity.Video;
import com.youtubeagent.repository.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoManagementService videoManagementService;
    private final VideoRepository videoRepository;
    private final YoutubeUploadService youtubeUploadService;

    public VideoController(VideoManagementService videoManagementService,
                            VideoRepository videoRepository,
                            YoutubeUploadService youtubeUploadService) {
        this.videoManagementService = videoManagementService;
        this.videoRepository = videoRepository;
        this.youtubeUploadService = youtubeUploadService;
    }

    @GetMapping
    public ResponseEntity<Page<VideoDto>> listVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(videoManagementService.listVideos(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoDto> getVideo(@PathVariable Long id) {
        VideoDto dto = videoManagementService.getVideo(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<VideoDto> reuploadVideo(@PathVariable Long id) {
        Video video = videoRepository.findById(id).orElse(null);
        if (video == null || video.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }

        YoutubeUploadService.UploadResult result = youtubeUploadService.upload(
                video.getFilePath(),
                video.getTitle(),
                video.getDescription(),
                video.getTagsArray()
        );

        video.setYoutubeId(result.videoId());
        video.setYoutubeUrl(result.url());
        video.setStatus("uploaded");
        video.setUploadedAt(java.time.LocalDateTime.now());
        videoRepository.save(video);

        return ResponseEntity.ok(videoManagementService.toDto(video));
    }
}
