package com.thacbao.recommendationservice.client;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.recommendationservice.dto.InteractionBatchDTO;
import com.thacbao.recommendationservice.dto.UserInteractionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", fallbackFactory = OrderServiceFallbackFactory.class)
public interface OrderServiceClient {

    @GetMapping("/internal/orders/user/{userId}/interaction-products")
    ApiResponse<UserInteractionDTO> getUserInteractionProducts(@PathVariable Integer userId);

    @GetMapping("/internal/orders/interactions/reviews")
    ApiResponse<InteractionBatchDTO> getReviewInteractions(@RequestParam int page, @RequestParam int size);

    @GetMapping("/internal/orders/interactions/orders")
    ApiResponse<InteractionBatchDTO> getOrderInteractions(@RequestParam int page, @RequestParam int size);

    @GetMapping("/internal/orders/interactions/wishlists")
    ApiResponse<InteractionBatchDTO> getWishlistInteractions(@RequestParam int page, @RequestParam int size);
}
