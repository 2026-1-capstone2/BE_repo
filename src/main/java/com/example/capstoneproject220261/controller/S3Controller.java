package com.example.capstoneproject220261.controller;

import com.example.capstoneproject220261.service.S3Service;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {

  private final S3Service s3Service;

  @GetMapping("/presigned-url")
  public ResponseEntity<Map<String, String>> getPresignedUrl(
      @RequestParam String fileName) {
    String url = s3Service.generatePresignedUrl(fileName);

    Map<String, String> response = new HashMap<>();
    response.put("url", url);
    response.put("fileName", fileName);

    return ResponseEntity.ok(response);
  }
}
