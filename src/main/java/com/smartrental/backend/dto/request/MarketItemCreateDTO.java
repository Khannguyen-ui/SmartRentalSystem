package com.smartrental.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MarketItemCreateDTO {
    @NotBlank(message = "Tiêu đề không được trống")
    private String title;

    private String description;

    @NotNull(message = "Giá không được trống (nhập 0 nếu tặng)")
    private BigDecimal price;

    private String category; // FURNITURE, ELECTRONIC, OTHER

    private List<String> images; // Link ảnh Cloudinary
}