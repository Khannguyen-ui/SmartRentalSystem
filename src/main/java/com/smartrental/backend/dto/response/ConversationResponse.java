package com.smartrental.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ConversationResponse {
    private Long id;            // ID của đối phương (Partner ID) - Dùng để click vào chat
    private String fullName;    // Tên đối phương
    private String avatar;      // Avatar đối phương
    private String lastMessage; // Tin nhắn cuối cùng
    private LocalDateTime lastTime; // Thời gian nhắn
    private boolean isOnline;   // (Tuỳ chọn, tạm thời để false)
    private int unreadCount;    // (Tuỳ chọn, tạm thời để 0)
}