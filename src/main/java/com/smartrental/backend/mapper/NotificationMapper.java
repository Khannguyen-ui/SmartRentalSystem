package com.smartrental.backend.mapper;

import com.smartrental.backend.dto.response.NotificationResponse;
import com.smartrental.backend.entity.Notification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    @Autowired
    private ModelMapper modelMapper;

    public NotificationResponse toResponse(Notification entity) {
        // 1. Dùng ModelMapper để map các trường cơ bản (title, message, type...)
        NotificationResponse dto = modelMapper.map(entity, NotificationResponse.class);

        // 2. [QUAN TRỌNG] Map thủ công referenceId để đảm bảo không bao giờ bị Null
        if (entity.getReferenceId() != null) {
            dto.setReferenceId(entity.getReferenceId());
        }

        return dto;
    }
}