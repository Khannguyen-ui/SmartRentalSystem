package com.smartrental.backend.dto.request;

import com.smartrental.backend.entity.Room; // Import Enum từ Entity
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

    private BigDecimal deposit;

    @NotNull(message = "Diện tích không được trống")
    private Double area;

    @NotBlank(message = "Địa chỉ không được trống")
    private String address;

    @NotNull(message = "Vĩ độ (Lat) không được trống")
    private Double latitude;

    @NotNull(message = "Kinh độ (Long) không được trống")
    private Double longitude;

    // --- CẬP NHẬT CHO HYBRID ---
    @NotNull(message = "Phải chọn loại hình thuê (WHOLE/SHARED)")
    private Room.RentalType rentalType;

    @NotNull(message = "Phải nhập sức chứa")
    private Integer capacity;

    private Room.GenderConstraint genderConstraint; // MALE_ONLY, FEMALE_ONLY, MIXED
    // ----------------------------

    private List<String> images;
    private List<String> amenities;
}