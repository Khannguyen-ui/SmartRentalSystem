package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.ReviewCreateDTO;
import com.smartrental.backend.entity.Review;
import java.util.List;

public interface ReviewService {
    Review createReview(ReviewCreateDTO dto);
    List<Review> getRoomReviews(Long roomId);
    Review replyToReview(Long reviewId, String replyContent);
}