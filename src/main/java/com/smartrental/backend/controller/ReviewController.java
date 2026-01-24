package com.smartrental.backend.controller;

import com.smartrental.backend.dto.request.ReviewCreateDTO;
import com.smartrental.backend.entity.Review;
import com.smartrental.backend.service.ReviewService; // Import Interface
import com.smartrental.backend.service.impl.ReviewServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService; // Chỉ gọi Service

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody @Valid ReviewCreateDTO dto) {
        return ResponseEntity.ok(reviewService.createReview(dto));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Review>> getRoomReviews(@PathVariable Long roomId) {
        return ResponseEntity.ok(reviewService.getRoomReviews(roomId));
    }
    // Trong ReviewController.java [cite: 331-338]

    @PutMapping("/{id}/reply")
    public ResponseEntity<Review> replyToReview(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> payload) {
        return ResponseEntity.ok(reviewService.replyToReview(id, payload.get("reply")));
    }
}