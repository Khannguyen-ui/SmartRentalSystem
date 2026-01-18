package com.smartrental.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
// import com.fasterxml.jackson.annotation.JsonFormat; // <--- Xóa hoặc Comment dòng này

@Data
public class AppointmentCreateDTO {
    @NotNull(message = "Phải chọn phòng")
    private Long roomId;

    @NotNull(message = "Phải chọn giờ hẹn")
    // --- XÓA hoặc COMMENT DÒNG NÀY ---
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // ---------------------------------

    // Mặc định Jackson sẽ hiểu chuỗi "2025-01-18T14:30:00" là LocalDateTime chuẩn
    private LocalDateTime meetTime;

    private String message;
}