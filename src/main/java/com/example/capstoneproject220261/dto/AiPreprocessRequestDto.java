package com.example.capstoneproject220261.dto;

public record AiPreprocessRequestDto(
    String jobId,
    String userId,
    String video_url,
    Metadata metadata
) {
  public record Metadata(
      Integer video_duration_sec,
      String video_resolution,
      String uploaded_at
  ) {}
}
