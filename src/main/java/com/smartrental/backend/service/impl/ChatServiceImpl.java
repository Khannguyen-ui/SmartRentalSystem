package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ChatMessageDTO;
import com.smartrental.backend.entity.Conversation;
import com.smartrental.backend.entity.Message;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.ConversationRepository;
import com.smartrental.backend.repository.MessageRepository;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl { // Bạn tự tạo interface nếu muốn

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public Message saveMessage(ChatMessageDTO dto) {
        // 1. Tìm hoặc tạo hội thoại mới
        Conversation conversation = conversationRepository.findExistingConversation(dto.getSenderId(), dto.getReceiverId())
                .orElseGet(() -> {
                    User u1 = userRepository.findById(dto.getSenderId()).orElseThrow();
                    User u2 = userRepository.findById(dto.getReceiverId()).orElseThrow();
                    return Conversation.builder()
                            .user1(u1)
                            .user2(u2)
                            .lastMessage(dto.getContent())
                            .updatedAt(LocalDateTime.now())
                            .build();
                });

        // Cập nhật tin nhắn cuối
        conversation.setLastMessage(dto.getContent());
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation savedConv = conversationRepository.save(conversation);

        // 2. Lưu tin nhắn
        User sender = userRepository.findById(dto.getSenderId()).orElseThrow();

        Message message = Message.builder()
                .conversation(savedConv)
                .sender(sender)
                .content(dto.getContent())
                .type(dto.getType()) // TEXT hoặc IMAGE
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }
}