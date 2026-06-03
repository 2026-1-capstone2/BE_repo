package com.example.capstoneproject220261.service;

import com.example.capstoneproject220261.domain.Video;
import com.example.capstoneproject220261.dto.VideoUploadedRequestDto;
import com.example.capstoneproject220261.repository.VideoRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoCommandService {

  private final VideoRepository videoRepository;

  @Transactional
  public Video saveVideoIdempotent(VideoUploadedRequestDto request) {
    // 1차 방어
    Optional<Video> existing = videoRepository.findByS3Key(request.s3Key());
    if (existing.isPresent()) {
      log.info("이미 등록된 영상 - 기존 jobId 반환: {}", existing.get().getJobId());
      return existing.get();
    }

    String jobId = UUID.randomUUID().toString();
    Video video = Video.builder()
                       .jobId(jobId)
                       .userId("anonymous")
                       .originalFilename(request.originalFilename())
                       .s3Key(request.s3Key())
                       .fileSizeBytes(request.fileSize())
                       .build();

    // 2차 방어
    try {
      Video saved = videoRepository.save(video);
      log.info("영상 저장 성공 - id: {}, jobId: {}, s3Key: {}",
          saved.getId(), saved.getJobId(), saved.getS3Key());
      return saved;
    } catch (DataIntegrityViolationException e) {
      log.info("동시 요청 감지 - 기존 영상 반환");
      return videoRepository.findByS3Key(request.s3Key())
                            .orElseThrow(() -> new IllegalStateException("멱등 처리 실패", e));
    }
  }
}
