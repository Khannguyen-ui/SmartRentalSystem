package com.smartrental.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentCreateDTO {
    @NotNull(message = "Phải chọn phòng")
    private Long roomId;

    @NotNull(message = "Phải chọn giờ hẹn")
    private LocalDateTime meetTime;

    private String message;
}