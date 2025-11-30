package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ChatMessageDTO;
import com.smartrental.backend.dto.response.ChatMessageResponse;
import com.smartrental.backend.entity.Message;
import com.smartrental.backend.mapper.ChatMapper;
import com.smartrental.backend.service.impl.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatServiceImpl chatService;
    private final ChatMapper chatMapper; // Inject thêm Mapper

    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessage) {
        // 1. Lưu vào Database
        Message savedMsg = chatService.saveMessage(chatMessage);

        // 2. Convert sang DTO (QUAN TRỌNG: Cắt bỏ các quan hệ Proxy gây lỗi)
        ChatMessageResponse response = chatMapper.toResponse(savedMsg);

        // 3. Gửi DTO xuống Client
        messagingTemplate.convertAndSend(
                "/topic/user/" + chatMessage.getReceiverId(),
                response
        );
    }
}