package com.smartrental.backend.mapper;

import com.smartrental.backend.dto.request.ChatMessageDTO;
import com.smartrental.backend.dto.response.ChatMessageResponse;
import com.smartrental.backend.entity.Message;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    @Autowired
    private ModelMapper modelMapper;

    // Request -> Entity
    public Message toEntity(ChatMessageDTO dto) {
        return modelMapper.map(dto, Message.class);
    }

    // Entity -> Response DTO (FIX LỖI PROXY Ở ĐÂY)
    public ChatMessageResponse toResponse(Message entity) {
        ChatMessageResponse dto = new ChatMessageResponse();
        dto.setId(entity.getId());
        dto.setContent(entity.getContent());
        dto.setType(entity.getType());
        dto.setCreatedAt(entity.getCreatedAt());

        // Lấy ID trần, không lấy object User để tránh lỗi Lazy Load
        dto.setSenderId(entity.getSender().getId());

        // Logic tìm receiverId từ Conversation
        Long u1 = entity.getConversation().getUser1().getId();
        Long u2 = entity.getConversation().getUser2().getId();
        Long senderId = entity.getSender().getId();

        // Nếu người gửi là u1 thì người nhận là u2 và ngược lại
        dto.setReceiverId(u1.equals(senderId) ? u2 : u1);

        return dto;
    }
}