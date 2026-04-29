package com.example.capstoneproject220261.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.region.static}")
  private String region;

  public String generatePresignedUrl(String fileName) {

    String key = UUID.randomUUID() + "_" + fileName;

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                                                        .bucket(bucket)
                                                        .key(key)
                                                        .build();

    try (S3Presigner resigned = S3Presigner.builder()
                                           .region(Region.of(region))
                                           .credentialsProvider(
                                                s3Client.serviceClientConfiguration()
                                                        .credentialsProvider())
                                           .build()) {

      PresignedPutObjectRequest presignedRequest =
          resigned.presignPutObject(r -> r
              .signatureDuration(Duration.ofMinutes(10))
              .putObjectRequest(putObjectRequest));

      return presignedRequest.url().toString();
    }
  }
}