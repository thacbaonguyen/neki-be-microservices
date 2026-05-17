package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.dto.request.*;
import com.thacbao.orderservice.dto.response.OrderResponse;
import com.thacbao.orderservice.dto.response.OrderSummaryResponse;
import com.thacbao.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/from-cart")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrderFromCart(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrderFromCart(request, userId)));
    }

    @PostMapping("/from-selected")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrderFromSelected(
            @Valid @RequestBody CreateSelectedOrderRequest request,
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.createOrderFromSelectedItems(request.getOrderRequest(), request.getOrderItemRequests(), userId)));
    }

    @PostMapping("/buy-now")
    public ResponseEntity<ApiResponse<OrderResponse>> buyNow(
            @Valid @RequestBody BuyNowRequest request,
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.buyNow(request.getOrderRequest(), request.getOrderItemRequest(), userId)));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getMyOrders(
            @RequestHeader("X-User-Id") Integer userId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(userId, pageable)));
    }

    @GetMapping("/my-orders/status/{status}")
    public ResponseEntity<ApiResponse<Page<OrderSummaryResponse>>> getMyOrdersByStatus(
            @PathVariable String status,
            @RequestHeader("X-User-Id") Integer userId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrdersByStatus(userId, status, pageable)));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Integer orderId,
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(orderId, userId)));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber,
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderByOrderNumber(orderNumber, userId)));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Integer orderId,
            @RequestBody CancelOrderRequest request,
            @RequestHeader("X-User-Id") Integer userId) {
        orderService.cancelOrder(orderId, request.getReason(), userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{orderId}/reorder")
    public ResponseEntity<ApiResponse<OrderResponse>> reOrder(
            @PathVariable Integer orderId,
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.reOrder(orderId, userId)));
    }

    @GetMapping("/track")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(
            @RequestParam String orderNumber, @RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success(orderService.trackOrder(orderNumber, email)));
    }
}
