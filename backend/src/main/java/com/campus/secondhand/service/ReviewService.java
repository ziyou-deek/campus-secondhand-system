package com.campus.secondhand.service;

import com.campus.secondhand.dto.ReviewRequest;
import com.campus.secondhand.entity.Review;

import java.util.List;

public interface ReviewService {
    Review create(ReviewRequest request);
    List<Review> listByProduct(Long productId);
    List<Review> listByUser(Long userId);
}
