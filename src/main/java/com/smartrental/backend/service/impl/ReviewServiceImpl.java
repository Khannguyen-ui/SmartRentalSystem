package com.smartrental.backend.service.impl;

import com.smartrental.backend.dto.request.ReviewCreateDTO;
import com.smartrental.backend.dto.response.ReviewResponseDTO;
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
    private final NotificationService notificationService;

    @Override
    @Transactional
    public ReviewResponseDTO createReview(ReviewCreateDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tenant = userRepository.findByEmail(email).orElseThrow();
        Room room = roomRepository.findById(dto.getRoomId()).orElseThrow();

        if (!contractRepository.existsByTenantIdAndRoomId(tenant.getId(), room.getId())) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá phòng sau khi đã thuê.");
        }
        if (reviewRepository.existsByTenantIdAndRoomId(tenant.getId(), room.getId())) {
            throw new RuntimeException("Mỗi phòng chỉ được đánh giá một lần.");
        }

        Review review = Review.builder()
                .room(room)
                .tenant(tenant)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .reviewImages(dto.getReviewImages())
                .build();
        Review saved = reviewRepository.save(review);

        updateRoomRatingAggregation(room.getId());

        notificationService.sendNotification(
                room.getLandlord(),
                "⭐ Đánh giá mới",
                "Phòng '" + room.getTitle() + "' vừa nhận được đánh giá " + saved.getRating() + " sao từ " + tenant.getFullName(),
                NotificationType.REVIEW_NEW,
                saved.getId()
        );

        // Trả về DTO thay vì Entity
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public ReviewResponseDTO replyToReview(Long reviewId, String replyContent) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow();

        if (!review.getRoom().getLandlord().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền phản hồi đánh giá này.");
        }

        review.setLandlordReply(replyContent);
        review.setRepliedAt(LocalDateTime.now());
        Review updated = reviewRepository.save(review);

        notificationService.sendNotification(
                review.getTenant(),
                "💬 Phản hồi đánh giá",
                "Chủ trọ đã phản hồi bình luận của bạn tại phòng '" + review.getRoom().getTitle() + "'",
                NotificationType.REVIEW_REPLY,
                review.getId()
        );

        // Trả về DTO thay vì Entity
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getRoomReviews(Long roomId) {
        List<Review> reviews = reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId);
        return reviews.stream().map(this::mapToResponseDTO).toList();
    }

    // --- HÀM PHỤ ĐỂ MAP DỮ LIỆU TẬP TRUNG ---
    private ReviewResponseDTO mapToResponseDTO(Review rev) {
        return ReviewResponseDTO.builder()
                .id(rev.getId())
                .tenantName(rev.getTenant().getFullName()) // Lấy từ User entity
                .tenantAvatar(rev.getTenant().getAvatarUrl()) // Lấy đúng getAvatarUrl() từ User
                .rating(rev.getRating())
                .comment(rev.getComment())
                .reviewImages(rev.getReviewImages())
                .createdAt(rev.getCreatedAt())
                .landlordReply(rev.getLandlordReply())
                .repliedAt(rev.getRepliedAt())
                .build();
    }

    private void updateRoomRatingAggregation(Long roomId) {
        int total = reviewRepository.countByRoomId(roomId);
        Double avg = reviewRepository.getAverageRatingByRoomId(roomId);
        Room room = roomRepository.findById(roomId).orElseThrow();
        room.setAverageRating(Math.round((avg != null ? avg : 0.0) * 10.0) / 10.0);
        room.setTotalReviews(total);
        roomRepository.save(room);
    }
}