package com.smartrental.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryDTO {
    private String name;        // VD: "T1/25"
    private BigDecimal highest;
    private BigDecimal popular;
    private BigDecimal lowest;
}