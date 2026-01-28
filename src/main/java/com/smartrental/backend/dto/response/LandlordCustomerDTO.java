package com.smartrental.backend.dto.response;

import com.smartrental.backend.model.json.ServiceFee;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class LandlordCustomerDTO {
    private Long contractId;

    // 1. Thông tin khách
    private Long tenantId;
    private String tenantName;
    private String tenantPhone;
    private String tenantAvatar;
    private String tenantEmail;

    // 2. Thông tin phòng & Hợp đồng (Thêm các trường này để tránh bị rỗng ở UI)
    private Long roomId;
    private String roomTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, EXPIRED, CANCELLED, POTENTIAL

    private BigDecimal monthlyRent;   // Tiền thuê tháng
    private BigDecimal depositAmount; // Tiền cọc
    private BigDecimal electricPrice; // Đơn giá điện lúc ký
    private BigDecimal waterPrice;    // Đơn giá nước lúc ký
    private List<ServiceFee> serviceFees; // Phí dịch vụ (rác, wifi...)

    // 3. 🟢 THÔNG TIN HÓA ĐƠN (Lấy từ bảng bills)
    private List<BillSummaryDTO> bills;

    // Class con để chứa dữ liệu hóa đơn rút gọn
    @Data
    @Builder
    public static class BillSummaryDTO {
        private Long id;
        private int month;
        private int year;
        private int electricOld;
        private int electricNew;
        private int waterOld;
        private int waterNew;
        private BigDecimal totalAmount;
        private String status; // UNPAID, PAID...
    }
}