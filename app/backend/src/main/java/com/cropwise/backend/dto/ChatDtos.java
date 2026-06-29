package com.cropwise.backend.dto;

import com.cropwise.backend.model.ChatMessage;

public class ChatDtos {
    public record SaveMessageRequest(String role, String content, String sessionId) {}

    public record ChatMessageDto(String id, String role, String content,
                                 String sessionId, String createdAt) {
        public static ChatMessageDto from(ChatMessage m) {
            return new ChatMessageDto(m.getId(), m.getRole(), m.getContent(),
                    m.getSessionId(), m.getCreatedAt().toString());
        }
    }
}
