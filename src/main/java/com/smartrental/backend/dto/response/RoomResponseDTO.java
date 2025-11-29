package com.smartrental.backend.dto.response;

import com.smartrental.backend.entity.Room;
import lombok.Data;
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

    // --- CẬP NHẬT CHO HYBRID ---
    private Room.RentalType rentalType;
    private Integer capacity;
    private Integer currentTenants; // Để App hiện: "Còn 3/8 chỗ"
    private Room.GenderConstraint genderConstraint;
    // ----------------------------

    private List<String> images;
    private List<String> amenities;
    private String status;

    private String landlordName;
    private String landlordPhone;
}