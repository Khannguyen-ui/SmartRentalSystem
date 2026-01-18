package com.smartrental.backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentResponseDTO {
    private Long id;

    // Thông tin phòng
    private Long roomId;
    private String roomTitle;
    private String roomAddress;

    // Thông tin đối tác (Nếu mình là chủ -> đây là thông tin khách. Nếu mình là khách -> đây là thông tin chủ)
    private Long partnerId;
    private String partnerName;
    private String partnerPhone;
    private String partnerAvatar;

    private LocalDateTime meetTime;
    private String message;
    private String status; // PENDING, CONFIRMED, CANCELLED

    private LocalDateTime suggestedMeetTime;
    private boolean isMyRequest;

    private LocalDateTime createdAt;
}