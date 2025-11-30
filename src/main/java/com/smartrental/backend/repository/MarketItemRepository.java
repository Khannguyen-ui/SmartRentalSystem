package com.smartrental.backend.repository;

import com.smartrental.backend.entity.MarketItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketItemRepository extends JpaRepository<MarketItem, Long> {

    // 1. Lấy danh sách đồ đang bán (Status = AVAILABLE), sắp xếp mới nhất trước
    List<MarketItem> findByStatusOrderByCreatedAtDesc(String status);

    // 2. Lấy danh sách đồ của một người bán cụ thể (Để chủ đồ quản lý)
    List<MarketItem> findByUserId(Long userId);

    // 3. Tìm kiếm đồ cũ theo từ khóa (VD: "Bàn", "Ghế") và trạng thái
    @Query("SELECT m FROM MarketItem m WHERE m.status = 'AVAILABLE' AND (LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<MarketItem> searchItems(@Param("keyword") String keyword);

    // 4. Lọc theo danh mục (VD: FURNITURE)
    List<MarketItem> findByCategoryAndStatus(String category, String status);
}