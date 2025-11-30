package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.MarketItemDTO;
import java.util.List;

public interface MarketService {
    // Đăng bán đồ mới
    MarketItemDTO createItem(MarketItemDTO dto);

    // Lấy tất cả đồ đang bán (cho người mua xem)
    List<MarketItemDTO> getAllAvailableItems();

    // Tìm kiếm đồ cũ theo từ khóa
    List<MarketItemDTO> searchItems(String keyword);

    // Đánh dấu đã bán (Cập nhật người mua nếu có)
    void markAsSold(Long itemId, Long buyerId);
}