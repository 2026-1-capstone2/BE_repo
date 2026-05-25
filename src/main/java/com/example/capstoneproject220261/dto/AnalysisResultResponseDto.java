package com.example.capstoneproject220261.dto;

import com.example.capstoneproject220261.domain.AnalysisResult;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public record AnalysisResultResponseDto(
    String jobId,
    String status,              // "DONE" or "FAILED"
    Integer videoDurationSec,
    String videoResolution,
    Integer frameCount,
    Integer points3D,
    BigDecimal spatialConfidence,
    String estimatedType,
    BigDecimal estimatedAreaM2,
    List<String> detectedObjects,
    Integer preprocessMs,
    Integer totalMs,
    String errorMessage
) {

  public static AnalysisResultResponseDto done(AnalysisResult result) {
    return new AnalysisResultResponseDto(
        result.getVideo().getJobId(),
        "DONE",
        result.getVideoDurationSec(),
        result.getVideoResolution(),
        result.getFrameCount(),
        result.getPoints3D(),
        result.getSpatialConfidence(),
        result.getEstimatedType(),
        result.getEstimatedAreaM2(),
        result.getDetectedObjects(),
        result.getPreprocessMs(),
        result.getTotalMs(),
        null
    );
  }

  public static AnalysisResultResponseDto failed(String jobId, String errorMessage) {
    return new AnalysisResultResponseDto(
        jobId, "FAILED", null, null, null, null, null,
        null, null, Collections.emptyList(), null, null, errorMessage
    );
  }
}
