package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy tất cả review của 1 phòng để hiển thị
    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);
    // Tính điểm trung bình (xử lý null nếu chưa có đánh giá)
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.room.landlord.id = :landlordId")
    Double getAverageRatingByLandlord(@Param("landlordId") Long landlordId);

    // Đếm tổng số review
    @Query("SELECT COUNT(r) FROM Review r WHERE r.room.landlord.id = :landlordId")
    int countReviewsByLandlord(@Param("landlordId") Long landlordId);
}