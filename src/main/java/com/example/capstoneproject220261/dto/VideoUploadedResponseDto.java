package com.example.capstoneproject220261.dto;

import com.example.capstoneproject220261.domain.Video;

public record VideoUploadedResponseDto(
    String jobId,
    String status
) {
  public static VideoUploadedResponseDto from(Video video){
    return new VideoUploadedResponseDto(
        video.getJobId(),
        video.getStatus().name()
    );
  }
}
