package com.smartrental.backend.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class RoomResponseDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal deposit;
    private Double area;
    private String address;
    
    // Trả về tọa độ số để App hiển thị
    private Double latitude;
    private Double longitude;
    
    private String gender;
    private List<String> images;
    private List<String> amenities;
    private String status;
    
    private String landlordName;
    private String landlordPhone;
}