package com.cropwise.backend.service;

import com.cropwise.backend.dto.ChatDtos.*;
import com.cropwise.backend.model.ChatMessage;
import com.cropwise.backend.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository repo;

    public ChatService(ChatMessageRepository repo) { this.repo = repo; }

    public List<ChatMessageDto> history(String userId) {
        return repo.findByUserIdOrderByCreatedAtAsc(userId).stream().map(ChatMessageDto::from).toList();
    }

    public ChatMessageDto save(String userId, SaveMessageRequest req) {
        ChatMessage m = new ChatMessage();
        m.setUserId(userId);
        m.setRole(req.role());
        m.setContent(req.content());
        m.setSessionId(req.sessionId());
        return ChatMessageDto.from(repo.save(m));
    }

    @Transactional
    public void clear(String userId) { repo.deleteByUserId(userId); }
}
