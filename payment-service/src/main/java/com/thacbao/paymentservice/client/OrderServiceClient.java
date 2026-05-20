package com.thacbao.paymentservice.client;

import com.thacbao.common.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-service", fallbackFactory = OrderServiceFallbackFactory.class)
public interface OrderServiceClient {

    @PutMapping("/internal/orders/{orderNumber}/status")
    ApiResponse<Void> updateOrderStatus(@PathVariable String orderNumber, @RequestParam String status);
}
