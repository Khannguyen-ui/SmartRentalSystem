package com.smartrental.backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponseDTO {
    private Long id;
    
    // Thông tin phòng tóm tắt
    private Long roomId;
    private String roomTitle;
    private String roomAddress;
    
    // Thông tin người hẹn (hoặc chủ trọ tùy ngữ cảnh)
    private String partnerName; 
    private String partnerPhone;

    private LocalDateTime meetTime;
    private String message;
    private String status;
    private LocalDateTime createdAt;
}