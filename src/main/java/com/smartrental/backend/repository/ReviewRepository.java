package com.smartrental.backend.repository;

import com.smartrental.backend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy tất cả review của 1 phòng để hiển thị
    List<Review> findByRoomIdOrderByCreatedAtDesc(Long roomId);
}