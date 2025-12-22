package com.smartrental.backend.dto.request;

import com.smartrental.backend.entity.Room; // Import Entity Room để lấy Enum
import lombok.Data;
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

    // --- SỬA Ở ĐÂY: Dùng Enum thay vì String ---
    private Room.RentalType rentalType;
    private Integer capacity;
    private Room.GenderConstraint genderConstraint;
    // -------------------------------------------

    private String videoUrl;
    private Long servicePackageId;

    private List<String> images;
    private List<String> amenities;
}