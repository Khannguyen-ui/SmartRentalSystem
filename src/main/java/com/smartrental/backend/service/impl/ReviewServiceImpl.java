package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ReviewCreateDTO;
import com.smartrental.backend.entity.*;
import com.smartrental.backend.repository.*;
import com.smartrental.backend.service.ReviewService;
import com.smartrental.backend.service.NotificationService; // [cite: 830]
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final NotificationService notificationService; // [cite: 1117]

    @Override
    @Transactional
    public Review createReview(ReviewCreateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tenant = userRepository.findByEmail(email).orElseThrow();
        Room room = roomRepository.findById(dto.getRoomId()).orElseThrow();

        // 1. Kiểm tra điều kiện (đã thuê và chưa đánh giá)
        if (!contractRepository.existsByTenantIdAndRoomId(tenant.getId(), room.getId())) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá phòng sau khi đã thuê.");
        }
        if (reviewRepository.existsByTenantIdAndRoomId(tenant.getId(), room.getId())) {
            throw new RuntimeException("Mỗi phòng chỉ được đánh giá một lần.");
        }

        // 2. Lưu Review
        Review review = Review.builder()
                .room(room)
                .tenant(tenant)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .reviewImages(dto.getReviewImages())
                .build();
        Review saved = reviewRepository.save(review);

        // 3. Cập nhật số liệu trung bình cho phòng
        updateRoomRatingAggregation(room.getId());

        // 4. Gửi thông báo cho Chủ trọ [cite: 1118-1122]
        notificationService.sendNotification(
                room.getLandlord(),
                "⭐ Đánh giá mới",
                "Phòng '" + room.getTitle() + "' vừa nhận được đánh giá " + saved.getRating() + " sao từ " + tenant.getFullName(),
                NotificationType.REVIEW_NEW,
                saved.getId()
        );

        return saved;
    }

    @Transactional
    public Review replyToReview(Long reviewId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        // Kiểm tra xem người đang phản hồi có phải là chủ trọ của phòng này không [cite: 615]
        if (!review.getRoom().getLandlord().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền phản hồi đánh giá này.");
        }

        // Cập nhật phản hồi
        review.setLandlordReply(replyContent);
        review.setRepliedAt(LocalDateTime.now());
        Review updated = reviewRepository.save(review);

        // Gửi thông báo cho Khách thuê (Người đã viết review) [cite: 1118-1122]
        notificationService.sendNotification(
                review.getTenant(),
                "💬 Phản hồi đánh giá",
                "Chủ trọ đã phản hồi bình luận của bạn tại phòng '" + review.getRoom().getTitle() + "'",
                NotificationType.REVIEW_REPLY,
                review.getId()
        );

        return updated;
    }

    private void updateRoomRatingAggregation(Long roomId) {
        int total = reviewRepository.countByRoomId(roomId);
        Double avg = reviewRepository.getAverageRatingByRoomId(roomId);
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setAverageRating(Math.round(avg * 10.0) / 10.0);
        room.setTotalReviews(total);
        roomRepository.save(room);
    }

    @Override
    public List<Review> getRoomReviews(Long roomId) {
        return reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
    }
}