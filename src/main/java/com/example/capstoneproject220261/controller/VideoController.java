package com.example.capstoneproject220261.controller;

import com.example.capstoneproject220261.domain.Video;
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

  //Front가 영상을 올리고 작동됨.
  // 1) DB에 영상 저장, 2) AI 서버 전처리 의뢰, 3) JobId 반환(Front SSE 구독용)
  @PostMapping("/uploaded")
  public ResponseEntity<VideoUploadedResponseDto> notifyUploaded(
      @Valid @RequestBody VideoUploadedRequestDto request) {
    Video video = videoService.registerUploadedVideo(request);
    return ResponseEntity.ok(VideoUploadedResponseDto.from(video));
  }

  //MediaType.TEXT_EVENT_STREAM_VALUE는 SSE 전용 응답 타입이다.
  //Front가 SSE를 구독
  @GetMapping(value = "/{jobId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamResult(@PathVariable String jobId) {
    return sseEmitterService.subscribe(jobId);
  }
}
