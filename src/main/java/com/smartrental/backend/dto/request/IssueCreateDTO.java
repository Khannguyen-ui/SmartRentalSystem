package com.smartrental.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IssueCreateDTO {
    @NotNull(message = "Phải chọn hợp đồng")
    private Long contractId;

    @NotBlank(message = "Tiêu đề không được trống")
    private String title;

    private String description;

    private String imageUrl; // Frontend gửi link Cloudinary vào đây
}