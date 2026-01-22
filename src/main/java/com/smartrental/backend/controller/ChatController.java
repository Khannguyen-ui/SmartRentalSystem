package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ChatMessageDTO;
import com.smartrental.backend.dto.response.ChatMessageResponse;
import com.smartrental.backend.dto.response.ConversationResponse;
import com.smartrental.backend.entity.Message;
import com.smartrental.backend.mapper.ChatMapper;
import com.smartrental.backend.service.impl.ChatServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity; // <--- Import mới
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*; // <--- Import các annotation REST (GetMapping, PostMapping...)

import java.util.List;

@RestController
@RequestMapping("/api/chat") // <--- QUAN TRỌNG: Định nghĩa đường dẫn gốc cho HTTP
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatServiceImpl chatService;
    private final ChatMapper chatMapper;

    // ========================================================================
    // 1. WEBSOCKET HANDLER (Giữ nguyên code cũ của bạn)
    // ========================================================================
    // Client gửi qua socket: /app/chat
    @MessageMapping("/chat")
    public void processMessage(@Payload ChatMessageDTO chatMessage) {
        // 1. Lưu vào Database
        Message savedMsg = chatService.saveMessage(chatMessage);

        // 2. Convert sang DTO
        ChatMessageResponse response = chatMapper.toResponse(savedMsg);

        // 3. Gửi DTO xuống Client (Real-time)
        messagingTemplate.convertAndSend(
                "/topic/user/" + chatMessage.getReceiverId(),
                response
        );
    }

    // ========================================================================
    // 2. HTTP REST API (THÊM MỚI ĐỂ SỬA LỖI 404)
    // ========================================================================

    // API: Lấy lịch sử chat với một người
    // GET http://localhost:8080/api/chat/history/{partnerId}
    @GetMapping("/history/{partnerId}")
    public ResponseEntity<List<ChatMessageResponse>> getChatHistory(@PathVariable Long partnerId) {
        return ResponseEntity.ok(chatService.getChatHistory(partnerId));
    }

    // API: Gửi tin nhắn qua HTTP (Fallback hoặc dùng cho Mobile/Postman)
    // POST http://localhost:8080/api/chat/send
    @PostMapping("/send")
    public ResponseEntity<ChatMessageResponse> sendMessage(@RequestBody ChatMessageDTO dto) {
        // 1. Lưu tin nhắn
        Message savedMsg = chatService.saveMessage(dto);
        ChatMessageResponse response = chatMapper.toResponse(savedMsg);

        // 2. Vẫn bắn Socket để người kia nhận được ngay (Hybrid)
        messagingTemplate.convertAndSend(
                "/topic/user/" + dto.getReceiverId(),
                response
        );

        return ResponseEntity.ok(response);
    }
    // --- API MỚI 1: LẤY DANH SÁCH SIDEBAR ---
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getConversations() {
        return ResponseEntity.ok(chatService.getUserConversations());
    }

    // --- API MỚI 2: BẮT ĐẦU CHAT VỚI NGƯỜI LẠ ---
    @PostMapping("/start")
    public ResponseEntity<String> startChat(@RequestBody java.util.Map<String, Long> payload) {
        Long partnerId = payload.get("partnerId");
        chatService.createConversationIfNotExists(partnerId);
        return ResponseEntity.ok("Conversation started");
    }


}