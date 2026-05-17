package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.dto.request.*;
import com.thacbao.orderservice.dto.response.OrderResponse;
import com.thacbao.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            OrderFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders(filter, pageable)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Integer orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderByIdAdmin(orderId)));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Integer orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateOrderStatus(orderId, request.getStatus())));
    }

    @PutMapping("/bulk-status")
    public ResponseEntity<ApiResponse<Void>> bulkUpdateStatus(
            @RequestBody BulkUpdateOrderRequest request) {
        orderService.bulkUpdateStatus(request.getOrderIds(), request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportOrders(@RequestBody ExportOrderRequest request) {
        byte[] data = orderService.exportOrders(request.getOrderFilter(), request.getFormat());
        return ResponseEntity.ok(data);
    }
}
