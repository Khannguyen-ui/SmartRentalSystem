package com.smartrental.backend.service; // Đúng package service

import com.smartrental.backend.dto.response.PriceHistoryDTO;
import java.util.List;

public interface PriceStatisticsService {
    // Tên phương thức phải khớp chính xác
    List<PriceHistoryDTO> getPriceHistoryForRoom(Long roomId);
}