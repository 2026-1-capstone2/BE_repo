package com.example.capstoneproject220261.controller;

import com.example.capstoneproject220261.dto.ChatRequestDto;
import com.example.capstoneproject220261.dto.ChatResponseDto;
import com.example.capstoneproject220261.service.AiService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

  private final AiService aiService;

  @PostMapping
  public ResponseEntity<ChatResponseDto> chat(@RequestBody ChatRequestDto request) {

    ChatRequestDto withUser = new ChatRequestDto(
        request.job_id(),
        request.user_id() != null ? request.user_id() : "anonymous",
        request.question(),
        request.history() != null ? request.history() : List.of(),
        request.max_new_tokens(),
        request.temperature()
    );
    return ResponseEntity.ok(aiService.chat(withUser));
  }
}
