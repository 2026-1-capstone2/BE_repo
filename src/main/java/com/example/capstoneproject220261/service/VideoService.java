package com.example.capstoneproject220261.service;

import com.example.capstoneproject220261.domain.AnalysisResult;
import com.example.capstoneproject220261.domain.Video;
import com.example.capstoneproject220261.domain.Video.VideoStatus;
import com.example.capstoneproject220261.dto.AiPreprocessRequestDto;
import com.example.capstoneproject220261.dto.AnalysisCompletedMessageDto;
import com.example.capstoneproject220261.dto.AnalysisResultResponseDto;
import com.example.capstoneproject220261.dto.VideoUploadedRequestDto;
import com.example.capstoneproject220261.repository.AnalysisResultRepository;
import com.example.capstoneproject220261.repository.VideoRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoService {

  private final VideoRepository videoRepository;
  private final AnalysisResultRepository analysisResultRepository;
  private final S3Service s3Service;
  private final AiService aiService;
  private final SseEmitterService sseEmitterService;
  private final VideoCommandService videoCommandService;

  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public Video registerUploadedVideo(VideoUploadedRequestDto request) {
    Video saved = videoCommandService.saveVideoIdempotent(request);
    requestAiPreprocess(saved);
    return saved;
  }

  private void requestAiPreprocess(Video saved) {
    try {
      String videoUrl = s3Service.generateDownloadPresignedUrl(saved.getS3Key());
      AiPreprocessRequestDto aiRequest = new AiPreprocessRequestDto(
          saved.getJobId(),
          saved.getUserId(),
          videoUrl,
          new AiPreprocessRequestDto.Metadata(null, null, null)
      );
      aiService.preprocess(aiRequest);
      log.info("AI 서버 전처리 요청 완료 - jobId: {}", saved.getJobId());
    } catch (Exception e) {
      log.error("AI 서버 전처리 의뢰 실패 - jobId: {} (영상은 저장됨)", saved.getJobId());
    }
  }

  @Transactional
  public void handleAnalysisResult(AnalysisCompletedMessageDto message) {
    String jobId = message.job_id();
    log.info("분석 결과 수신 - jobId: {}, status: {}", jobId, message.status());

    Video video = videoRepository.findByJobId(jobId)
                                 .orElseThrow(() -> new IllegalArgumentException("영상 없음: " + jobId));

    if (message.isFailed()) {
      video.markAsFailed();
      videoRepository.save(video);

      String errorMsg = message.error() != null ? message.error().message() : "분석 실패";
      sseEmitterService.sendResult(jobId, AnalysisResultResponseDto.failed(jobId, errorMsg));
      return;
    }

    AnalysisResult analysisResult = AnalysisResult.builder()
                                                  .video(video)
                                                  .spatialFeaturesS3Key(message.spatial_features_s3_key())
                                                  .totalMs(message.duration_ms() != null ? message.duration_ms().intValue() : null)
                                                  .build();

    analysisResultRepository.save(analysisResult);
    video.markAsDone();
    videoRepository.save(video);

    log.info("분석 결과 저장 완료 - jobId: {}", jobId);
    sseEmitterService.sendResult(jobId, AnalysisResultResponseDto.done(analysisResult));
  }

  private BigDecimal toBigDecimal(Double value) {
    return value != null ? BigDecimal.valueOf(value) : null;
  }

  @Cacheable(value = "analysisResult", key = "#jobId", unless = "#result == null")
  public AnalysisResultResponseDto getAnalysisResult(String jobId) {
    log.info("분석 결과 조회(캐시 미스) - jobId: {}", jobId);

    Video video = videoRepository.findByJobId(jobId)
                                 .orElseThrow(() -> new IllegalArgumentException("영상 없음: " + jobId));

    if (video.getStatus() != VideoStatus.DONE) {
      return null;
    }

    AnalysisResult result = analysisResultRepository.findByVideo(video)
                                                    .orElseThrow(() -> new IllegalArgumentException("분석 결과 없음: " + jobId));

    return AnalysisResultResponseDto.done(result);
  }
}
