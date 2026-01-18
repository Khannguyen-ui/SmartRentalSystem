package com.smartrental.backend.service;

import com.smartrental.backend.dto.response.NotificationResponse;
import com.smartrental.backend.entity.NotificationType;
import com.smartrental.backend.entity.User;
import java.util.List;

public interface NotificationService {
    // 1. Gửi thông báo
    void sendNotification(User receiver, String title, String message, NotificationType type, Long referenceId);

    // 2. Lấy danh sách thông báo
    List<NotificationResponse> getMyNotifications();

    // 3. Đánh dấu đã đọc theo ID thông báo (Click vào thông báo)
    void markAsRead(Long id);

    // 4. [QUAN TRỌNG] Đánh dấu đã đọc theo Reference ID (Dùng khi xử lý logic nghiệp vụ như Accept/Reject lịch)
    void markAsReadByReference(Long referenceId, NotificationType type);
}