package com.smartrental.backend.dto.response;

import com.smartrental.backend.model.json.ServiceFee;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ContractResponseDTO {
    private Long id;

    // Thông tin phòng tóm tắt
    private Long roomId;
    private String roomTitle;
    private String roomAddress;

    // Thông tin người thuê
    private Long tenantId;
    private String tenantName;
    private String tenantEmail;
    private String tenantPhone;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;

    private List<ServiceFee> serviceFees; // Trả về danh sách phí
    private String status;
}