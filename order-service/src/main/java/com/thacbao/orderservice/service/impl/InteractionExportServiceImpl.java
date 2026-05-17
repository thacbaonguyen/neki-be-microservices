package com.thacbao.orderservice.service.impl;

import com.thacbao.orderservice.dto.response.InteractionBatchDTO;
import com.thacbao.orderservice.dto.response.UserInteractionDTO;
import com.thacbao.orderservice.model.OrderItem;
import com.thacbao.orderservice.model.Review;
import com.thacbao.orderservice.model.Wishlist;
import com.thacbao.orderservice.repository.OrderItemRepository;
import com.thacbao.orderservice.repository.ReviewRepository;
import com.thacbao.orderservice.repository.WishlistRepository;
import com.thacbao.orderservice.service.InteractionExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionExportServiceImpl implements InteractionExportService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final WishlistRepository wishlistRepository;

    @Override
    public UserInteractionDTO getUserInteractionProducts(Integer userId) {
        Set<Integer> productIds = new HashSet<>();
        
        // Orders
        int page = 0;
        while (true) {
            Page<OrderItem> orderPage = orderItemRepository.findByOrderUserId(userId, PageRequest.of(page, 100));
            if (orderPage.isEmpty()) break;
            
            orderPage.getContent().forEach(item -> {
                if (item.getProductId() != null) {
                    productIds.add(item.getProductId());
                }
            });
            
            if (!orderPage.hasNext()) break;
            page++;
        }
        
        // Wishlists
        Optional<Wishlist> wishlist = wishlistRepository.findByUserId(userId);
        wishlist.ifPresent(w -> w.getProductIds().forEach(productIds::add));
        
        // Reviews
        page = 0;
        while (true) {
            Page<Review> reviewPage = reviewRepository.findByUserIdAndRatingGreaterThanEqual(userId, 4, PageRequest.of(page, 100));
            if (reviewPage.isEmpty()) break;
            
            reviewPage.getContent().forEach(review -> {
                if (review.getProductId() != null) {
                    productIds.add(review.getProductId());
                }
            });
            
            if (!reviewPage.hasNext()) break;
            page++;
        }
        
        return UserInteractionDTO.builder()
                .userId(userId)
                .productIds(productIds)
                .build();
    }

    @Override
    public InteractionBatchDTO getReviewInteractions(int page, int size) {
        Page<Review> reviewPage = reviewRepository.findAll(PageRequest.of(page, size));
        Map<Integer, Map<Integer, Double>> scores = new HashMap<>();
        
        reviewPage.getContent().forEach(review -> {
            if (review.getRating() != null && review.getRating() >= 3 && review.getProductId() != null && review.getUserId() != null) {
                scores.computeIfAbsent(review.getUserId(), k -> new HashMap<>())
                        .merge(review.getProductId(), 1.0, Double::max);
            }
        });
        
        return InteractionBatchDTO.builder()
                .userProductScores(scores)
                .page(reviewPage.getNumber())
                .totalPages(reviewPage.getTotalPages())
                .hasNext(reviewPage.hasNext())
                .build();
    }

    @Override
    public InteractionBatchDTO getOrderInteractions(int page, int size) {
        Page<OrderItem> orderPage = orderItemRepository.findAll(PageRequest.of(page, size));
        Map<Integer, Map<Integer, Double>> scores = new HashMap<>();
        
        orderPage.getContent().forEach(item -> {
            if (item.getOrder() != null && item.getOrder().getUserId() != null && item.getProductId() != null) {
                Integer userId = item.getOrder().getUserId();
                Integer productId = item.getProductId();
                if (userId != null) {
                    scores.computeIfAbsent(userId, k -> new HashMap<>())
                            .merge(productId, 1.0, Double::max);
                }
            }
        });
        
        return InteractionBatchDTO.builder()
                .userProductScores(scores)
                .page(orderPage.getNumber())
                .totalPages(orderPage.getTotalPages())
                .hasNext(orderPage.hasNext())
                .build();
    }

    @Override
    public InteractionBatchDTO getWishlistInteractions(int page, int size) {
        Page<Wishlist> wishlistPage = wishlistRepository.findAll(PageRequest.of(page, size));
        Map<Integer, Map<Integer, Double>> scores = new HashMap<>();
        
        wishlistPage.getContent().forEach(wishlist -> {
            if (wishlist.getUserId() != null && wishlist.getProductIds() != null) {
                Integer userId = wishlist.getUserId();
                wishlist.getProductIds().forEach(productId -> {
                    scores.computeIfAbsent(userId, k -> new HashMap<>())
                            .merge(productId, 1.0, Double::max);
                });
            }
        });
        
        return InteractionBatchDTO.builder()
                .userProductScores(scores)
                .page(wishlistPage.getNumber())
                .totalPages(wishlistPage.getTotalPages())
                .hasNext(wishlistPage.hasNext())
                .build();
    }
}
