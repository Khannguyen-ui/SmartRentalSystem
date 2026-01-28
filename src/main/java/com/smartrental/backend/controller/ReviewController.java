package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ReviewCreateDTO;
import com.smartrental.backend.dto.response.ReviewResponseDTO; // Import DTO mới
import com.smartrental.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 1. Cập nhật kiểu trả về thành ReviewResponseDTO
    @PostMapping
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody @Valid ReviewCreateDTO dto) {
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    // 2. Cập nhật kiểu trả về thành List<ReviewResponseDTO> -> HẾT LỖI TẠI ĐÂY
    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<ReviewResponseDTO>> getRoomReviews(@PathVariable Long roomId) {
        return ResponseEntity.ok(reviewService.getRoomReviews(roomId));
    }

    // 3. Cập nhật kiểu trả về cho phần phản hồi
    @PutMapping("/{id}/reply")
    public ResponseEntity<ReviewResponseDTO> replyToReview(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(reviewService.replyToReview(id, payload.get("reply")));
    }
}