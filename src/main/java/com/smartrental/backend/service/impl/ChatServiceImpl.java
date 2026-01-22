package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ChatMessageDTO;
import com.smartrental.backend.dto.response.ChatMessageResponse;
import com.smartrental.backend.dto.response.ConversationResponse; // <--- Import DTO mới
import com.smartrental.backend.entity.Conversation;
import com.smartrental.backend.entity.Message;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.ChatMapper;
import com.smartrental.backend.repository.ConversationRepository;
import com.smartrental.backend.repository.MessageRepository;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;

    // --- Helper lấy User hiện tại từ Token ---
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ========================================================================
    // 1. CÁC HÀM CŨ (GIỮ NGUYÊN)
    // ========================================================================

    @Transactional
    public Message saveMessage(ChatMessageDTO dto) {
        // Tìm hội thoại, nếu chưa có thì tạo mới
        Conversation conversation = conversationRepository.findExistingConversation(dto.getSenderId(), dto.getReceiverId())
                .orElseGet(() -> {
                    User u1 = userRepository.findById(dto.getSenderId()).orElseThrow(() -> new RuntimeException("Sender not found"));
                    User u2 = userRepository.findById(dto.getReceiverId()).orElseThrow(() -> new RuntimeException("Receiver not found"));
                    return Conversation.builder()
                            .user1(u1)
                            .user2(u2)
                            .lastMessage(dto.getContent())
                            .updatedAt(LocalDateTime.now())
                            .build();
                });

        // Cập nhật tin nhắn cuối cùng
        conversation.setLastMessage(dto.getContent());
        conversation.setUpdatedAt(LocalDateTime.now());
        Conversation savedConv = conversationRepository.save(conversation);

        User sender = userRepository.findById(dto.getSenderId()).orElseThrow();

        // Lưu tin nhắn chi tiết
        Message message = Message.builder()
                .conversation(savedConv)
                .sender(sender)
                .content(dto.getContent())
                .type(dto.getType())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(message);
    }

    public List<ChatMessageResponse> getChatHistory(Long partnerId) {
        User currentUser = getCurrentUser();

        return conversationRepository.findExistingConversation(currentUser.getId(), partnerId)
                .map(conversation ->
                        messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId())
                                .stream()
                                .map(chatMapper::toResponse)
                                .collect(Collectors.toList())
                )
                .orElse(new ArrayList<>());
    }

    // ========================================================================
    // 2. CÁC HÀM MỚI CẬP NHẬT (CHO SIDEBAR & START CHAT)
    // ========================================================================

    /**
     * Lấy danh sách các cuộc hội thoại để hiển thị lên Sidebar bên trái
     * Sử dụng repository method: findMyConversations
     */
    public List<ConversationResponse> getUserConversations() {
        User currentUser = getCurrentUser();

        // Gọi hàm custom query trong Repository của bạn
        List<Conversation> conversations = conversationRepository.findMyConversations(currentUser.getId());

        return conversations.stream().map(c -> {
            // Xác định đối phương (Partner) là ai?
            // Nếu tôi là user1 thì đối phương là user2 và ngược lại
            User partner = (c.getUser1().getId().equals(currentUser.getId()))
                    ? c.getUser2()
                    : c.getUser1();

            // Map sang DTO Response
            return ConversationResponse.builder()
                    .id(partner.getId())            // ID đối phương (để FE gọi API lấy lịch sử)
                    .fullName(partner.getFullName()) // Tên đối phương
                    .avatar(partner.getAvatarUrl()) // Avatar đối phương (lưu ý tên getter trong Entity User của bạn)
                    .lastMessage(c.getLastMessage()) // Tin nhắn cuối
                    .lastTime(c.getUpdatedAt())      // Thời gian cập nhật
                    .isOnline(false)                 // TODO: Tích hợp Redis/Socket để check online thật
                    .unreadCount(0)                  // TODO: Tích hợp logic đếm tin chưa đọc
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * Tạo hội thoại mới nếu chưa tồn tại (Dùng cho nút "Chat với chủ nhà")
     */
    public void createConversationIfNotExists(Long partnerId) {
        User currentUser = getCurrentUser();

        // Kiểm tra xem đã có hội thoại chưa
        conversationRepository.findExistingConversation(currentUser.getId(), partnerId)
                .orElseGet(() -> {
                    User u1 = userRepository.findById(currentUser.getId()).orElseThrow(() -> new RuntimeException("User not found"));
                    User u2 = userRepository.findById(partnerId).orElseThrow(() -> new RuntimeException("Partner not found"));

                    Conversation newConv = Conversation.builder()
                            .user1(u1)
                            .user2(u2)
                            .lastMessage("Bắt đầu cuộc trò chuyện") // Tin nhắn khởi tạo
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return conversationRepository.save(newConv);
                });
    }
}