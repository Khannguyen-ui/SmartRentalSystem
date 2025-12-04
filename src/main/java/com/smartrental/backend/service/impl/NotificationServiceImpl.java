package com.smartrental.backend.service.impl;

import com.smartrental.backend.entity.Notification;
import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.NotificationRepository;
import com.smartrental.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.smartrental.backend.dto.response.NotificationResponse;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl { // Bạn tự tạo interface nếu cần

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // Dùng để bắn Socket
    private final ModelMapper modelMapper;

    // Hàm dùng nội bộ: Tạo và gửi thông báo
    @Transactional
    public void sendNotification(User receiver, String title, String message, NotificationType type) {
        // 1. Lưu vào Database
        Notification notification = Notification.builder()
                .user(receiver)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        Notification savedNoti = notificationRepository.save(notification);

        // 2. Convert sang DTO
        NotificationResponse response = modelMapper.map(savedNoti, NotificationResponse.class);

        // 3. Bắn Real-time qua WebSocket
        // Client sẽ subscribe: /topic/user/{userId}/notifications
        messagingTemplate.convertAndSend(
                "/topic/user/" + receiver.getId() + "/notifications",
                response
        );
    }

    // Lấy danh sách thông báo của tôi
    public List<NotificationResponse> getMyNotifications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(noti -> modelMapper.map(noti, NotificationResponse.class))
                .collect(Collectors.toList());
    }

    // Đánh dấu đã đọc
    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}