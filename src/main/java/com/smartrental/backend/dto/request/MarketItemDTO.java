package com.smartrental.backend.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MarketItemDTO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private List<String> images;
    private String status;

    // Thông tin người bán
    private Long sellerId;
    private String sellerName;
    private String sellerPhone;

    private LocalDateTime createdAt;
}