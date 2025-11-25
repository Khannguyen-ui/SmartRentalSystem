package com.smartrental.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomCreateDTO {
    @NotBlank(message = "Tiêu đề không được trống")
    private String title;
    
    private String description;

    @NotNull(message = "Giá không được trống")
    private BigDecimal price;
    
    private BigDecimal deposit; // Tiền cọc

    @NotNull(message = "Diện tích không được trống")
    private Double area;

    @NotBlank(message = "Địa chỉ không được trống")
    private String address;

    @NotNull(message = "Vĩ độ (Lat) không được trống")
    private Double latitude;

    @NotNull(message = "Kinh độ (Long) không được trống")
    private Double longitude;

    private String gender; // ALL, MALE, FEMALE

    private List<String> images;    // Link ảnh
    private List<String> amenities; // Danh sách tiện ích
}