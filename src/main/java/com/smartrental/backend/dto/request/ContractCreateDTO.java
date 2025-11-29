package com.smartrental.backend.dto.request;

import com.smartrental.backend.model.json.ServiceFee;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ContractCreateDTO {
    @NotNull(message = "Phải chọn phòng")
    private Long roomId;

    @NotNull(message = "Phải nhập email người thuê")
    private String tenantEmail;

    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;

    @NotNull private BigDecimal monthlyRent;
    private BigDecimal depositAmount;

    // Giá điện nước chốt lúc ký
    private BigDecimal electricPrice;
    private BigDecimal waterPrice;

    // Danh sách phí dịch vụ (JSON)
    private List<ServiceFee> serviceFees;
}