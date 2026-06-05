package com.example.capstoneproject220261.controller;

import com.example.capstoneproject220261.domain.Video;
import com.example.capstoneproject220261.dto.AnalysisResultResponseDto;
import com.example.capstoneproject220261.dto.VideoUploadedRequestDto;
import com.example.capstoneproject220261.dto.VideoUploadedResponseDto;
import com.example.capstoneproject220261.service.SseEmitterService;
import com.example.capstoneproject220261.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

  private final VideoService videoService;
  private final SseEmitterService sseEmitterService;

  @PostMapping("/uploaded")
  public ResponseEntity<VideoUploadedResponseDto> notifyUploaded(
      @Valid @RequestBody VideoUploadedRequestDto request) {
    Video video = videoService.registerUploadedVideo(request);
    return ResponseEntity.ok(VideoUploadedResponseDto.from(video));
  }

  @GetMapping(value = "/{jobId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamResult(@PathVariable String jobId) {
    return sseEmitterService.subscribe(jobId);
  }

  @GetMapping("/{jobId}/result")
  public ResponseEntity<AnalysisResultResponseDto> getResult(@PathVariable String jobId){
    AnalysisResultResponseDto result = videoService.getAnalysisResult(jobId);
    if(result == null) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(result);
  }
}
