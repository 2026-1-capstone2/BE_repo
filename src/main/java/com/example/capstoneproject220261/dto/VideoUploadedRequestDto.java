package com.example.capstoneproject220261.dto;

import jakarta.validation.constraints.NotBlank;

public record VideoUploadedRequestDto(

    @NotBlank(message = "S3Key는 필수입니다.")
    String s3Key,
    String originalFilename,
    Long fileSize
) {
}
