package com.smartrental.backend.service;

import com.smartrental.backend.dto.request.ReviewCreateDTO;
import com.smartrental.backend.dto.response.ReviewResponseDTO;
import com.smartrental.backend.entity.Review;
import java.util.List;

public interface ReviewService {

        ReviewResponseDTO createReview(ReviewCreateDTO dto);
        ReviewResponseDTO replyToReview(Long reviewId, String replyContent);
        List<ReviewResponseDTO> getRoomReviews(Long roomId);

}