package com.cropwise.backend.repository;

import com.cropwise.backend.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {
    List<ChatMessage> findByUserIdOrderByCreatedAtAsc(String userId);
    List<ChatMessage> findByUserIdAndSessionIdOrderByCreatedAtAsc(String userId, String sessionId);
    void deleteByUserId(String userId);
}
