package com.example.capstoneproject220261.service;

import static tools.jackson.databind.ext.javatime.util.DecimalUtils.toBigDecimal;

import com.example.capstoneproject220261.domain.AnalysisResult;
import com.example.capstoneproject220261.domain.Video;
import com.example.capstoneproject220261.dto.AiPreprocessRequestDto;
import com.example.capstoneproject220261.dto.AnalysisCompletedMessageDto;
import com.example.capstoneproject220261.dto.AnalysisResultResponseDto;
import com.example.capstoneproject220261.dto.VideoUploadedRequestDto;
import com.example.capstoneproject220261.repository.AnalysisResultRepository;
import com.example.capstoneproject220261.repository.VideoRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

  private final VideoRepository videoRepository;
  private final AnalysisResultRepository analysisResultRepository;
  private final S3Service s3Service;
  private final AiService aiService;
  private final SseEmitterService sseEmitterService;

  // FE에서 사용자가 업로드 했을 경우
  @Transactional
  public Video registerUploadedVideo(VideoUploadedRequestDto request) {
    String jobId = UUID.randomUUID().toString();

    Video video = Video.builder()
        .jobId(jobId)
        .userId("anonymous")
        .originalFilename(request.originalFilename())
        .s3Key(request.s3Key())
        .fileSizeBytes(request.fileSize())
        .build();

    Video saved = videoRepository.save(video);
    log.info("영상 저장 성공 - id: {}, jobId: {}, s3Key: {}", saved.getId(), saved.getJobId(), saved.getS3Key());

    String videoUrl = s3Service.generateDownloadPresignedUrl(saved.getS3Key());

    AiPreprocessRequestDto aiRequest = new AiPreprocessRequestDto(
        saved.getJobId(),
        saved.getUserId(),
        videoUrl,
        new AiPreprocessRequestDto.Metadata(null, null, null)
    );

    try { //성공 시에 State Default 값이 PROCESSING이니 따로 MARK 안해도 됨.
      aiService.preprocess(aiRequest);
      log.info("AI 서버 전처리 요청 완료 - jobId: {}", saved.getJobId());
    } catch (Exception e) {
      log.error("AI 서버 전처리 의뢰 실패 - jobId: {}", saved.getJobId());
      saved.markAsFailed();
      videoRepository.save(saved); //실패한 거 UPDATE 후 다시 저장.
      throw new IllegalStateException("AI 서버 요청 실패", e);
    }
    return saved; //FRONT에서 받아서 SSE 구독을 해야하니 RETURN 해줘야 함.
  }

  //AI 서버로부터 결과가 왔을 때
  @Transactional
  public void handleAnalysisResult(AnalysisCompletedMessageDto message) {
    String jobId = message.job_id();
    log.info("분석 결과 수신 - jobId: {}, status: {}", jobId, message.status());

    Video video = videoRepository.findByJobId(jobId)
                                 .orElseThrow(
                                     () -> new IllegalArgumentException("영상 없음: " + jobId));

    if(message.isFailed()) {
      video.markAsFailed();
      videoRepository.save(video);

      String errorMsg = message.error() != null ? message.error().message() : "분석 실패";
      sseEmitterService.sendResult(jobId, AnalysisResultResponseDto.failed(jobId, errorMsg));
    }

    AnalysisCompletedMessageDto.Result r = message.result();
    AnalysisCompletedMessageDto.VideoMetadata vm = r.video_metadata();
    AnalysisCompletedMessageDto.SpaceAnalysis sa = r.space_analysis();
    AnalysisCompletedMessageDto.ProcessingTime pt = r.processing_time();

    AnalysisResult analysisResult = AnalysisResult.builder()
                                                  .video(video)
                                                  .spatialFeaturesS3Key(r.spatial_features_s3_key())
                                                  .spatialConfidence(toBigDecimal(r.spatial_confidence()))
                                                  .videoDurationSec(vm != null ? vm.duration_sec() : null)
                                                  .videoResolution(vm != null ? vm.resolution() : null)
                                                  .frameCount(vm != null ? vm.frame_count() : null)
                                                  .points3D(vm != null ? vm.points_3d() : null)
                                                  .estimatedType(sa != null ? sa.estimated_type() : null)
                                                  .estimatedAreaM2(sa != null ? toBigDecimal(sa.estimated_area_m2()) : null)
                                                  .detectedObjects(sa != null ? sa.detected_objects() : null)
                                                  .preprocessMs(pt != null ? pt.preprocess_ms() : null)
                                                  .totalMs(pt != null ? pt.total_ms() : null)
                                                  .build();

    analysisResultRepository.save(analysisResult);
    video.markAsDone();
    videoRepository.save(video);

    log.info("분석 결과 저장 완료 - jobId: {}", jobId);

    // SSE로 프론트에 전송
    sseEmitterService.sendResult(jobId, AnalysisResultResponseDto.done(analysisResult));
  }

  private BigDecimal toBigDecimal(Double value) {
    return value != null ? BigDecimal.valueOf(value) : null;
  }
}
