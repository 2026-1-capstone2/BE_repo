package com.example.capstoneproject220261.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "videos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "job_id", unique = true, nullable = false, length = 50)
  private String jobId;

  // FIDO+PASSKEY 도입 후 사용 예정.
  @Column(name = "user_id", length = 100)
  private String userId;

  @Column(name = "original_filename", length = 255)
  private String originalFilename;

  @Column(name = "s3_key", nullable = false, length = 500)
  private String s3Key;

  @Column(name = "file_size_bytes")
  private Long fileSizeBytes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private VideoStatus status;

  @CreationTimestamp
  @Column(name = "uploaded_at", nullable = false, updatable = false)
  private LocalDateTime uploadedAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "processed_at")
  private LocalDateTime processedAt;

  @Builder
  private Video(String jobId, String userId, String originalFilename,
      String s3Key, Long fileSizeBytes) {
    this.jobId = jobId;
    this.userId = userId;
    this.originalFilename = originalFilename;
    this.s3Key = s3Key;
    this.fileSizeBytes = fileSizeBytes;
    this.status = VideoStatus.PROCESSING;
  }

  public void markAsDone() {
    this.status = VideoStatus.DONE;
    this.processedAt = LocalDateTime.now();
  }

  public void markAsFailed() {
    this.status = VideoStatus.FAILED;
    this.processedAt = LocalDateTime.now();
  }

  public enum VideoStatus {
    UPLOADED,    // S3 업로드 완료
    PROCESSING,  // AI 처리 진행 중
    DONE,        // 처리 완료
    FAILED       // 실패
  }
}
