package com.smartrental.backend.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillResponseDTO {
    private Long id;

    // Thông tin ngữ cảnh
    private Long contractId;
    private String roomTitle; // Để biết bill của phòng nào
    private String tenantName; // Để biết thu tiền ai

    private int month;
    private int year;

    // Chỉ số điện nước
    private int electricOld;
    private int electricNew;
    private int waterOld;
    private int waterNew;

    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}