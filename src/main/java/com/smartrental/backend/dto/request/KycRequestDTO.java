package com.smartrental.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class KycRequestDTO {
    @NotBlank(message = "Vui lòng nhập số CCCD/CMND")
    private String citizenId;

    @NotEmpty(message = "Vui lòng upload ít nhất 2 ảnh (Mặt trước & sau)")
    private List<String> citizenImages; // Mảng chứa URL ảnh
}