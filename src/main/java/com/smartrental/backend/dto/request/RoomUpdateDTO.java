package com.smartrental.backend.dto.request;

import com.smartrental.backend.entity.Room; // Import Entity Room để lấy Enum
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomUpdateDTO {
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal deposit;
    private Double area;
    private String address;
    private Double latitude;
    private Double longitude;
    //quy mo contact detail
    private String furnitureStatus;
    private String legalStatus;
    private String direction;
    private Integer floorNumber;
    private Integer numBedrooms;
    private Integer numBathrooms;

    // --- SỬA Ở ĐÂY: Dùng Enum thay vì String ---
    private Room.RentalType rentalType;
    private Integer capacity;
    private Room.GenderConstraint genderConstraint;
    // -------------------------------------------

    private String videoUrl;
    private Long servicePackageId;

    private List<String> images;
    private List<String> amenities;
    // --- (QUAN TRỌNG) THÔNG TIN CHỦ TRỌ ĐỂ HIỂN THỊ CARD ---
    private Long landlordId;          // Để chat
    private String landlordName;
    private String landlordPhone;
    private String landlordAvatar;
    // Avatar
    private LocalDateTime landlordJoinDate; // Ngày tham gia "5 năm trên nhà tốt"
    // -------------------------------------------------------

    private LocalDateTime createdAt; // Ngày đăng bài ("Cập nhật 3 giờ trước")

}