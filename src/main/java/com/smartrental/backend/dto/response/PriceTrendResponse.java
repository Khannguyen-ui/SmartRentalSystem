package com.smartrental.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceTrendResponse {
    private BigDecimal currentRoomPrice; // Giá hiện tại của chính phòng đó
    private List<PriceHistoryDTO> history; // Dữ liệu thị trường xung quanh
}