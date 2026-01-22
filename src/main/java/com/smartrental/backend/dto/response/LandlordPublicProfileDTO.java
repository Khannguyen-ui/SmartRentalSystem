package com.smartrental.backend.dto.response;

import lombok.Data;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
public class LandlordPublicProfileDTO {
    // 1. Thông tin cơ bản
    private Long id;
    private String fullName;
    private String avatarUrl;
    private LocalDateTime joinDate;
    private LocalDateTime lastActiveAt;
    private String bannerUrl;
    @JsonProperty("isIdentityVerified")
    private boolean isIdentityVerified;
    // 2. Chỉ số uy tín (Thống kê)
    private int totalRooms;               // Tổng số phòng đã đăng
    private int successfulDeals;          // Số hợp đồng đã ký thành công
    private int totalReviews;             // Tổng số lượt đánh giá
    private double averageRating;         // Điểm đánh giá trung bình (VD: 4.5)

    // 3. Thông tin bổ sung
    private List<String> activeDistricts; // Các quận hoạt động chính (VD: ["Quận 1", "Bình Thạnh"])
}