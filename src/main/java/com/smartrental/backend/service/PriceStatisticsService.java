package com.smartrental.backend.service;

import com.smartrental.backend.dto.response.PriceTrendResponse;

public interface PriceStatisticsService {
    // Chỉ giữ lại một phương thức duy nhất này
    PriceTrendResponse getPriceHistoryForRoom(Long roomId);
}