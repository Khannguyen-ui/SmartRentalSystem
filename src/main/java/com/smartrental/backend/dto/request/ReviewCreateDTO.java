package com.smartrental.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ReviewCreateDTO {
    @NotNull private Long roomId;

    // ID người dùng lấy từ Token, nhưng ở đây nhận tạm để test
    // Thực tế nên lấy từ SecurityContextHolder
    private Long tenantId;

    @Min(1) @Max(5)
    private Integer rating;

    private String comment;
    private List<String> reviewImages; // Danh sách link ảnh
}