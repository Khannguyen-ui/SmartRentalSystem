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
    private LocalDateTime meetTime;

    private Long tenantId;


    private String message;
}