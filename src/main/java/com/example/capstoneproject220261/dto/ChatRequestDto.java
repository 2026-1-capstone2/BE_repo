package com.example.capstoneproject220261.dto;

import java.util.List;

public record ChatRequestDto(
    String job_id,
    String user_id,
    String question,
    List<ChatMessage> history,
    Integer max_new_tokens,
    Double temperature
) {
  public record ChatMessage(String role, String content) {}
}
