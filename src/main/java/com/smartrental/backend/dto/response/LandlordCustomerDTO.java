package com.smartrental.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class LandlordCustomerDTO {
    private Long contractId;

    // Thông tin khách
    private Long tenantId;
    private String tenantName;
    private String tenantPhone;
    private String tenantAvatar;

    // Thông tin phòng & Hợp đồng
    private String roomTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, EXPIRED, CANCELLED
}