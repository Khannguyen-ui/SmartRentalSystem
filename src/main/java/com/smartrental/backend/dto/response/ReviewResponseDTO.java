package com.smartrental.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReviewResponseDTO {
    private Long id;
    private String tenantName;    // Lấy từ tenant.getFullName()
    private String tenantAvatar;  // Lấy từ tenant.getAvatar()
    private Double rating;
    private String comment;
    private List<String> reviewImages;
    private LocalDateTime createdAt;
    private String landlordReply;
    private LocalDateTime repliedAt;
}