package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy tất cả review của 1 phòng để hiển thị [cite: 784]
    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    // [MỚI] Kiểm tra xem người dùng đã đánh giá phòng này chưa
    boolean existsByTenantIdAndRoomId(Long tenantId, Long roomId);

    // Tính điểm trung bình của chủ trọ [cite: 785]
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.room.landlord.id = :landlordId")
    Double getAverageRatingByLandlord(@Param("landlordId") Long landlordId);

    // Đếm tổng số review của chủ trọ [cite: 786]
    @Query("SELECT COUNT(r) FROM Review r WHERE r.room.landlord.id = :landlordId")
    int countReviewsByLandlord(@Param("landlordId") Long landlordId);

    // [MỚI] Các hàm thống kê riêng cho từng phòng
    @Query("SELECT COUNT(r) FROM Review r WHERE r.room.id = :roomId")
    int countByRoomId(@Param("roomId") Long roomId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.room.id = :roomId")
    Double getAverageRatingByRoomId(@Param("roomId") Long roomId);
}