package com.smartrental.backend.repository;

import com.smartrental.backend.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    // Lấy lịch sử của user, sắp xếp mới nhất trước
    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(Long userId);

    // Kiểm tra xem đã tìm từ khóa này chưa (để tránh lưu trùng lặp)
    Optional<SearchHistory> findByUserIdAndQueryText(Long userId, String queryText);

    // Xóa tất cả lịch sử của 1 user
    void deleteByUserId(Long userId);
}