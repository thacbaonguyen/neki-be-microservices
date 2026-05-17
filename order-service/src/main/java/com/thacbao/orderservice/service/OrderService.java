package com.thacbao.orderservice.service;

import com.thacbao.orderservice.dto.request.OrderFilterRequest;
import com.thacbao.orderservice.dto.request.OrderItemRequest;
import com.thacbao.orderservice.dto.request.OrderRequest;
import com.thacbao.orderservice.dto.response.OrderResponse;
import com.thacbao.orderservice.dto.response.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    // User methods
    OrderResponse createOrderFromCart(OrderRequest request, Integer userId);

    OrderResponse createOrderFromSelectedItems(OrderRequest request, List<OrderItemRequest> items, Integer userId);

    OrderResponse buyNow(OrderRequest request, OrderItemRequest item, Integer userId);

    OrderResponse getOrderById(Integer orderId, Integer userId);

    OrderResponse getOrderByOrderNumber(String orderNumber, Integer userId);

    Page<OrderSummaryResponse> getMyOrders(Integer userId, Pageable pageable);

    Page<OrderSummaryResponse> getMyOrdersByStatus(Integer userId, String status, Pageable pageable);

    void cancelOrder(Integer orderId, String reason, Integer userId);

    OrderResponse reOrder(Integer orderId, Integer userId);

    // Admin methods
    Page<OrderResponse> getAllOrders(OrderFilterRequest filter, Pageable pageable);

    OrderResponse getOrderByIdAdmin(Integer orderId);

    OrderResponse updateOrderStatus(Integer orderId, String status);

    OrderResponse updateOrderStatus(String orderNumber, String status);

    void bulkUpdateStatus(List<Integer> orderIds, String status);

    byte[] exportOrders(OrderFilterRequest filter, String format);

    // Order tracking
    OrderResponse trackOrder(String orderNumber, String email);

    // Utility
    void validateOrder(List<OrderItemRequest> items);

    String generateOrderNumber();

    void restoreInventory(Integer variantId, Integer quantity);
}
