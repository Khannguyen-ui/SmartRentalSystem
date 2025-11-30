package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ReviewCreateDTO;
import com.smartrental.backend.entity.Review;
import com.smartrental.backend.entity.Room;
import com.smartrental.backend.entity.User;
import com.smartrental.backend.repository.ReviewRepository;
import com.smartrental.backend.repository.RoomRepository;
import com.smartrental.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    // 1. Viết đánh giá
    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody @Valid ReviewCreateDTO dto) {
        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng không tồn tại"));

        // Lấy User từ Token (hoặc từ DTO nếu chưa config Security chuẩn)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User tenant = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = Review.builder()
                .room(room)
                .tenant(tenant)
                .rating(dto.getRating())
                .comment(dto.getComment())
                .reviewImages(dto.getReviewImages()) // List URL từ Cloudinary
                .build();

        return ResponseEntity.ok(reviewRepository.save(review));
    }

    // 2. Xem đánh giá của 1 phòng
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Review>> getRoomReviews(@PathVariable Long roomId) {
        return ResponseEntity.ok(reviewRepository.findByRoomIdOrderByCreatedAtDesc(roomId));
    }
}