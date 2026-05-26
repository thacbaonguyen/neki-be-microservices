package com.thacbao.orderservice.service.impl;

import com.thacbao.common.exception.NotFoundException;
import com.thacbao.common.exception.PermissionException;
import com.thacbao.orderservice.client.ProductServiceClient;
import com.thacbao.orderservice.client.UserServiceClient;
import com.thacbao.orderservice.dto.request.ReviewRequest;
import com.thacbao.orderservice.dto.response.ReviewResponse;
import com.thacbao.orderservice.model.Order;
import com.thacbao.orderservice.model.Review;
import com.thacbao.orderservice.repository.OrderRepository;
import com.thacbao.orderservice.repository.ReviewRepository;
import com.thacbao.orderservice.service.ReviewService;
import com.thacbao.common.dto.UserDTO;
import com.thacbao.common.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @Override
    public ReviewResponse create(ReviewRequest request, Integer userId, String userFullName) {
        // Enforce 1 review per user per product
        if (reviewRepository.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new PermissionException("Bạn đã đánh giá sản phẩm này rồi");
        }

        // Verify purchase exists
        Optional<Order> orderOpt = orderRepository.findOrdersUserCanReview(
                userId, request.getProductId(), PageRequest.of(0, 1)).stream().findFirst();
        
        if (orderOpt.isEmpty()) {
            throw new PermissionException("Bạn phải mua sản phẩm mới có quyền đánh giá");
        }

        // Fetch user name if empty
        if (userFullName == null || userFullName.isBlank() || "unknown".equalsIgnoreCase(userFullName)) {
            try {
                ApiResponse<UserDTO> userResponse = userServiceClient.getUserById(userId);
                if (userResponse != null && userResponse.getData() != null) {
                    userFullName = userResponse.getData().getFullName();
                } else {
                    userFullName = "Khách Hàng";
                }
            } catch (Exception e) {
                log.warn("Failed to fetch user name for review. UserID: {}", userId);
                userFullName = "Khách Hàng";
            }
        }

        Review review = Review.builder()
                .productId(request.getProductId())
                .userId(userId)
                .userFullName(userFullName)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .order(orderOpt.get())
                .isVerifiedPurchase(true)
                .build();

        Review saved = reviewRepository.save(review);

        // Update product rating via Feign
        updateProductRating(request.getProductId());

        return ReviewResponse.from(saved);
    }

    @Override
    public ReviewResponse update(Integer id, ReviewRequest request, Integer userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found: " + id));

        if (!review.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setComment(request.getComment());
        Review saved = reviewRepository.save(review);

        updateProductRating(review.getProductId());

        return ReviewResponse.from(saved);
    }

    @Override
    public void delete(Integer id, Integer userId) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found: " + id));

        if (!review.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        Integer productId = review.getProductId();
        reviewRepository.delete(review);

        updateProductRating(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviewByProduct(Integer productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(ReviewResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviewsAdmin(Pageable pageable) {
        return reviewRepository.findAll(pageable)
                .map(ReviewResponse::from);
    }

    private void updateProductRating(Integer productId) {
        try {
            Object[] stats = reviewRepository.getRatingStats(productId);
            if (stats != null && stats.length >= 2) {
                BigDecimal avgRating = stats[0] instanceof BigDecimal ? (BigDecimal) stats[0]
                        : BigDecimal.valueOf(((Number) stats[0]).doubleValue());
                int count = ((Number) stats[1]).intValue();
                productServiceClient.updateRating(productId, avgRating, count);
            }
        } catch (Exception e) {
            log.warn("Failed to update rating for product {}: {}", productId, e.getMessage());
        }
    }
}
