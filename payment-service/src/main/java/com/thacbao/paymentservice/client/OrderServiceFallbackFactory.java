package com.thacbao.paymentservice.client;

import com.thacbao.common.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderServiceFallbackFactory implements FallbackFactory<OrderServiceClient> {

    @Override
    public OrderServiceClient create(Throwable cause) {
        log.error("Order Service unavailable: {}", cause.getMessage());
        return new OrderServiceClient() {
            @Override
            public ApiResponse<Void> updateOrderStatus(String orderNumber, String status) {
                log.warn("Fallback: updateOrderStatus for order={}", orderNumber);
                return ApiResponse.error(503, "Order Service unavailable");
            }
        };
    }
}
