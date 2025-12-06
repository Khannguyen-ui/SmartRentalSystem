package com.smartrental.backend.dto.request;

import com.smartrental.backend.entity.Room;
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

    // --- CẤU HÌNH HYBRID ---
    @NotNull(message = "Phải chọn loại hình thuê (WHOLE/SHARED)")
    private Room.RentalType rentalType;

    @NotNull(message = "Phải nhập sức chứa")
    private Integer capacity;

    private Room.GenderConstraint genderConstraint;

    // --- BỔ SUNG CÁC TRƯỜNG BỊ THIẾU ---

    private String videoUrl; // Link video Cloudinary (Optional)

    @NotNull(message = "Phải chọn gói đăng tin")
    private Integer servicePackageId; // ID gói cước để trừ tiền

    // ------------------------------------

    private List<String> images;    // Link ảnh
    private List<String> amenities; // Danh sách tiện ích
}