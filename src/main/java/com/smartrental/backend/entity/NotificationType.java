package com.smartrental.backend.entity;

public enum NotificationType {
    BILL_NEW,           // Có hóa đơn mới
    CONTRACT_SIGN,      // Yêu cầu ký hợp đồng
    ISSUE_UPDATE,       // Sự cố đã được xử lý
    CHAT_NEW,           // Tin nhắn mới (Optional)
    SYSTEM              // Thông báo hệ thống
}