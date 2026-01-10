package com.smartrental.backend.dto.response;

import com.smartrental.backend.entity.Room;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomResponseDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal deposit;
    private Double area;
    private String address;

    private Double latitude;
    private Double longitude;

    private Room.RentalType rentalType;
    private Integer capacity;
    private Integer currentTenants;
    private Room.GenderConstraint genderConstraint;

    private List<String> images;
    private List<String> amenities;
    private String status;
    private String videoUrl; // Thêm video

    // --- CÁC TRƯỜNG ĐẶC ĐIỂM CHI TIẾT (MỚI) ---
    private String furnitureStatus;
    private String legalStatus;
    private String direction;
    private Integer floorNumber;
    private Integer numBedrooms;
    private Integer numBathrooms;
    // ------------------------------------------

    // Thông tin chủ trọ
    private Long landlordId;
    private String landlordName;
    private String landlordPhone;
    private String landlordAvatar;
    private LocalDateTime landlordJoinDate;

    // Ngày đăng tin (Quan trọng để hiện thời gian)
    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;
}