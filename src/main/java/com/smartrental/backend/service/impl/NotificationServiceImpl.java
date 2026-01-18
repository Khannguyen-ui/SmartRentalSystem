package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.response.NotificationResponse;
import com.smartrental.backend.entity.Notification;
import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.mapper.NotificationMapper;
import com.smartrental.backend.repository.NotificationRepository;
import com.smartrental.backend.repository.UserRepository;
import com.smartrental.backend.service.NotificationService; // Import Interface
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService { // <-- Đã thêm implements

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void sendNotification(User receiver, String title, String message, NotificationType type, Long referenceId) {
        Notification notification = Notification.builder()
                .user(receiver)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();

        Notification savedNoti = notificationRepository.save(notification);

        // Map sang Response và bắn Socket
        NotificationResponse response = notificationMapper.toResponse(savedNoti);
        messagingTemplate.convertAndSend(
                "/topic/user/" + receiver.getId() + "/notifications",
                response
        );
    }

    @Override
    public List<NotificationResponse> getMyNotifications() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(notificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));

        // Kiểm tra nếu chưa đọc thì mới set (tối ưu hóa)
        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    // --- [HÀM MỚI ĐỂ FIX LỖI F5] ---
    @Override
    @Transactional
    public void markAsReadByReference(Long referenceId, NotificationType type) {
        // 1. Tìm tất cả thông báo liên quan đến ReferenceID (ví dụ ID lịch hẹn) và Type (ví dụ SUGGESTION)
        List<Notification> notifications = notificationRepository.findAllByReferenceIdAndType(referenceId, type);

        if (notifications.isEmpty()) return;

        boolean needSave = false;
        for (Notification notif : notifications) {
            if (!notif.isRead()) { // Chỉ xử lý cái chưa đọc
                notif.setRead(true);
                needSave = true;
            }
        }

        // 2. Lưu lại vào DB nếu có thay đổi
        if (needSave) {
            notificationRepository.saveAll(notifications);
        }
    }
}