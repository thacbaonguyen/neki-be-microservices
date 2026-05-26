package com.thacbao.recommendationservice.client;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.recommendationservice.dto.InteractionBatchDTO;
import com.thacbao.recommendationservice.dto.UserInteractionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class OrderServiceFallbackFactory implements FallbackFactory<OrderServiceClient> {

    @Override
    public OrderServiceClient create(Throwable cause) {
        log.error("Order Service unavailable: {}", cause.getMessage());
        return new OrderServiceClient() {
            @Override
            public ApiResponse<UserInteractionDTO> getUserInteractionProducts(Integer userId) {
                log.warn("Fallback: getUserInteractionProducts for user={}", userId);
                return ApiResponse.success(UserInteractionDTO.builder()
                        .userId(userId)
                        .productIds(Collections.emptySet())
                        .build());
            }

            @Override
            public ApiResponse<InteractionBatchDTO> getReviewInteractions(int page, int size) {
                log.warn("Fallback: getReviewInteractions page={}", page);
                return ApiResponse.success(InteractionBatchDTO.builder()
                        .userProductScores(Collections.emptyMap())
                        .page(page).totalPages(0).hasNext(false).build());
            }

            @Override
            public ApiResponse<InteractionBatchDTO> getOrderInteractions(int page, int size) {
                log.warn("Fallback: getOrderInteractions page={}", page);
                return ApiResponse.success(InteractionBatchDTO.builder()
                        .userProductScores(Collections.emptyMap())
                        .page(page).totalPages(0).hasNext(false).build());
            }

            @Override
            public ApiResponse<InteractionBatchDTO> getWishlistInteractions(int page, int size) {
                log.warn("Fallback: getWishlistInteractions page={}", page);
                return ApiResponse.success(InteractionBatchDTO.builder()
                        .userProductScores(Collections.emptyMap())
                        .page(page).totalPages(0).hasNext(false).build());
            }
        };
    }
}
