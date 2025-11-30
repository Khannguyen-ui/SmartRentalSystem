package com.smartrental.backend.dto.request;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private Long senderId;
    private Long receiverId; // Người nhận (Quan trọng)
    private String content;
    private String type; // TEXT, IMAGE
}