package com.smartrental.backend.entity;

public enum NotificationType {
    BILL_NEW,           // Có hóa đơn mới
    CONTRACT_SIGN,      // Yêu cầu ký hợp đồng
    ISSUE_UPDATE,       // Sự cố đã được xử lý
    CHAT_NEW,           // Tin nhắn mới
    SYSTEM,             // Thông báo hệ thống
    APPOINTMENT_SUGGESTION ,// <--- [MỚI] Đề xuất giờ xem phòng
    REVIEW_NEW,  //Dánh giá
    REVIEW_REPLY ,//Trả lời đánh giá
    PURCHASE_PACKAGE,
}