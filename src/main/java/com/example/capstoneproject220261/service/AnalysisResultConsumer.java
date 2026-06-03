package com.example.capstoneproject220261.service;

import com.example.capstoneproject220261.config.RabbitMQConfig;
import com.example.capstoneproject220261.domain.Video;
import com.example.capstoneproject220261.dto.AnalysisCompletedMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisResultConsumer {

  private final VideoService videoService;

  @RabbitListener(queues = RabbitMQConfig.COMPLETED_QUEUE)
  public void onAnalysisCompleted(AnalysisCompletedMessageDto message) {
    try {
      log.info("RabbitMQ 결과 수신 - jobId: {}, status: {}",
          message.job_id(), message.status());
      videoService.handleAnalysisResult(message);
    } catch (Exception e) {
      log.error("결과 메시지 처리 실패 - jobId: {}",
          message != null ? message.job_id() : "null", e);
    }
  }
}
