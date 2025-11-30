    package com.smartrental.backend.dto.response;

    import lombok.Data;
    import java.time.LocalDateTime;

    @Data
    public class ChatMessageResponse {
        private Long id;
        private Long senderId;
        private Long receiverId;
        private String content;
        private String type;
        private LocalDateTime createdAt;
    }