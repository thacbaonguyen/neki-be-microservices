package com.thacbao.orderservice.service;

import com.thacbao.orderservice.dto.request.ReviewRequest;
import com.thacbao.orderservice.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    ReviewResponse create(ReviewRequest request, Integer userId, String userFullName);

    ReviewResponse update(Integer id, ReviewRequest request, Integer userId);

    void delete(Integer id, Integer userId);

    Page<ReviewResponse> getAllReviewByProduct(Integer productId, Pageable pageable);

    Page<ReviewResponse> getAllReviewsAdmin(Pageable pageable);
}
