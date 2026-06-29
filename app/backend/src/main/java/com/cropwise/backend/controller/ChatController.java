package com.cropwise.backend.controller;

import com.cropwise.backend.dto.ChatDtos.*;
import com.cropwise.backend.security.CurrentUser;
import com.cropwise.backend.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chat;
    public ChatController(ChatService chat) { this.chat = chat; }

    @GetMapping("/history")
    public List<ChatMessageDto> history() { return chat.history(CurrentUser.id()); }

    @PostMapping("/messages")
    public ChatMessageDto save(@RequestBody SaveMessageRequest req) { return chat.save(CurrentUser.id(), req); }

    @DeleteMapping("/history")
    public void clear() { chat.clear(CurrentUser.id()); }
}
