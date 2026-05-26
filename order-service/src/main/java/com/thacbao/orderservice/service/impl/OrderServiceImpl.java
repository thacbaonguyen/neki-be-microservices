package com.thacbao.orderservice.service.impl;

import com.thacbao.common.dto.ProductVariantDTO;
import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.common.enums.OrderStatus;
import com.thacbao.common.event.OrderCancelledEvent;
import com.thacbao.common.event.OrderCreatedEvent;
import com.thacbao.common.event.OrderStatusUpdatedEvent;
import com.thacbao.common.exception.InvalidException;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.orderservice.client.ProductServiceClient;
import com.thacbao.orderservice.client.UserServiceClient;
import com.thacbao.orderservice.dto.request.OrderFilterRequest;
import com.thacbao.orderservice.dto.request.OrderItemRequest;
import com.thacbao.orderservice.dto.request.OrderRequest;
import com.thacbao.orderservice.dto.response.OrderResponse;
import com.thacbao.orderservice.dto.response.OrderSummaryResponse;
import com.thacbao.orderservice.model.Cart;
import com.thacbao.orderservice.model.CartItem;
import com.thacbao.orderservice.model.Discount;
import com.thacbao.orderservice.model.Order;
import com.thacbao.orderservice.model.OrderItem;
import com.thacbao.orderservice.repository.CartRepository;
import com.thacbao.orderservice.repository.OrderRepository;
import com.thacbao.orderservice.service.DiscountCalculationService;
import com.thacbao.orderservice.service.DiscountService;
import com.thacbao.orderservice.service.OrderService;
import com.thacbao.orderservice.service.ShippingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;
    private final DiscountService discountService;
    private final DiscountCalculationService discountCalculationService;
    private final ShippingService shippingService;
    private final OrderEventPublisher eventPublisher;

    @Override
    public OrderResponse createOrderFromCart(OrderRequest request, Integer userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user: " + userId));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        List<OrderItemRequest> items = cart.getCartItems().stream()
                .map(ci -> OrderItemRequest.builder()
                        .variantId(ci.getVariantId())
                        .quantity(ci.getQuantity())
                        .build())
                .toList();

        Order order = createOrder(request, items, userId);

        // Clear cart after order
        cart.getCartItems().clear();
        cartRepository.save(cart);

        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse createOrderFromSelectedItems(OrderRequest request, List<OrderItemRequest> items, Integer userId) {
        Order order = createOrder(request, items, userId);
        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse buyNow(OrderRequest request, OrderItemRequest item, Integer userId) {
        Order order = createOrder(request, List.of(item), userId);
        return OrderResponse.from(order);
    }

    private Order createOrder(OrderRequest request, List<OrderItemRequest> items, Integer userId) {
        // 1. Get user info via Feign
        var userResponse = userServiceClient.getUserById(userId);
        String userEmail = userResponse.getData() != null ? userResponse.getData().getEmail() : "";
        String userFullName = userResponse.getData() != null ? userResponse.getData().getFullName() : "";

        // 2. Get variant info via Feign
        List<Integer> variantIds = items.stream().map(OrderItemRequest::getVariantId).toList();
        ApiResponse<List<ProductVariantDTO>> variantsResponse = productServiceClient.getVariantsByIds(variantIds);
        List<ProductVariantDTO> variants = variantsResponse.getData();

        if (variants == null || variants.isEmpty()) {
            throw new NotFoundException("Variants not found");
        }

        Map<Integer, ProductVariantDTO> variantMap = variants.stream()
                .collect(Collectors.toMap(ProductVariantDTO::getId, v -> v));

        // 3. Reserve inventory
        List<Map<String, Integer>> reserveItems = items.stream()
                .map(i -> Map.of("variantId", i.getVariantId(), "quantity", i.getQuantity()))
                .toList();
        productServiceClient.reserveInventory(reserveItems);

        try {
            // 4. Calculate amounts
            BigDecimal totalAmount = BigDecimal.ZERO;
            Set<OrderItem> orderItems = new HashSet<>();
    
            for (OrderItemRequest itemReq : items) {
                ProductVariantDTO variant = variantMap.get(itemReq.getVariantId());
                if (variant == null) {
                    throw new NotFoundException("Variant not found: " + itemReq.getVariantId());
                }
    
                BigDecimal price = variant.getSalePrice() != null && variant.getSalePrice().compareTo(BigDecimal.ZERO) > 0
                        ? variant.getSalePrice() : variant.getPrice();
                BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
    
                OrderItem orderItem = OrderItem.builder()
                        .variantId(variant.getId())
                        .productId(variant.getProductId())
                        .productName(variant.getProductName())
                        .colorName(variant.getColorName())
                        .sizeName(variant.getSizeName())
                        .imageUrl(variant.getImageUrl())
                        .quantity(itemReq.getQuantity())
                        .unitPrice(price)
                        .totalPrice(itemTotal)
                        .build();
                orderItems.add(orderItem);
            }
    
            // 5. Calculate shipping & discount
            BigDecimal shippingFee = shippingService.calculateShippingFee(
                    request.getDistrict(), request.getWard(), totalAmount);
            BigDecimal discountAmount = BigDecimal.ZERO;
    
            Discount discount = null;
            if (StringUtils.hasText(request.getDiscountCode())) {
                discount = discountService.validateAndGetDiscount(
                        request.getDiscountCode(), userId, totalAmount);
                var discountResult = discountCalculationService.applyDiscountCode(
                        request.getDiscountCode(), userId, totalAmount, shippingFee);
                discountAmount = discountResult.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
    
            BigDecimal finalAmount = totalAmount.add(shippingFee).subtract(discountAmount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }
    
            // 6. Create order
            Order order = Order.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .userFullName(userFullName)
                    .orderNumber(generateOrderNumber())
                    .totalAmount(totalAmount)
                    .shippingFee(shippingFee)
                    .discountAmount(discountAmount)
                    .finalAmount(finalAmount)
                    .province(request.getProvince())
                    .district(request.getDistrict())
                    .ward(request.getWard())
                    .addressDetail(request.getAddressDetail())
                    .note(request.getNote())
                    .phoneDelivery(request.getPhoneDelivery())
                    .paymentMethodId(request.getPaymentMethodId())
                    .status(OrderStatus.PENDING)
                    .build();
    
            Order savedOrder = orderRepository.save(order);
    
            // Set order reference and save items
            orderItems.forEach(oi -> oi.setOrder(savedOrder));
            savedOrder.setOrderItems(orderItems);
            orderRepository.save(savedOrder);
    
            // 7. Record discount usage
            if (discount != null) {
                discountService.recordUsage(discount, userId, savedOrder);
            }
    
            // 8. Update total sold on Product Service
            for (OrderItemRequest itemReq : items) {
                ProductVariantDTO variant = variantMap.get(itemReq.getVariantId());
                if (variant != null) {
                    try {
                        productServiceClient.updateTotalSold(variant.getProductId(), itemReq.getQuantity());
                    } catch (Exception e) {
                        log.warn("Failed to update totalSold for product {}: {}", variant.getProductId(), e.getMessage());
                    }
                }
            }
    
            // 9. Publish event (now handled by outbox)
            publishOrderCreatedEvent(savedOrder);
    
            return savedOrder;
        } catch (Exception e) {
            log.error("Failed to create order, executing compensating transaction to restore inventory...", e);
            try {
                productServiceClient.restoreInventory(reserveItems);
                log.info("Successfully restored inventory for failed order creation.");
            } catch (Exception restoreEx) {
                log.error("FATAL: Failed to execute compensatory inventory restore. Phantom locks may exist!", restoreEx);
            }
            throw e; // Rethrow to let @Transactional rollback local DB
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer orderId, Integer userId) {
        Order order = orderRepository.findByIdWithFullDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber, Integer userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNumber));
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }
        return OrderResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrders(Integer userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable).map(OrderSummaryResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> getMyOrdersByStatus(Integer userId, String status, Pageable pageable) {
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        return orderRepository.findByUserIdAndStatus(userId, orderStatus, pageable)
                .map(OrderSummaryResponse::from);
    }

    @Override
    public void cancelOrder(Integer orderId, String reason, Integer userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only cancel PENDING orders");
        }

        String oldStatus = order.getStatus().getValue();
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Restore inventory
        List<Map<String, Integer>> restoreItems = order.getOrderItems().stream()
                .map(oi -> Map.of("variantId", oi.getVariantId(), "quantity", oi.getQuantity()))
                .toList();
        try {
            productServiceClient.restoreInventory(restoreItems);
        } catch (Exception e) {
            log.error("Failed to restore inventory for order {}: {}", orderId, e.getMessage());
        }

        // Publish cancelled event
        publishOrderCancelledEvent(order, reason);
    }

    @Override
    public OrderResponse reOrder(Integer orderId, Integer userId) {
        Order originalOrder = orderRepository.findByIdWithFullDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        if (!originalOrder.getUserId().equals(userId)) {
            throw new SecurityException("Access denied");
        }

        List<OrderItemRequest> items = originalOrder.getOrderItems().stream()
                .map(oi -> OrderItemRequest.builder()
                        .variantId(oi.getVariantId())
                        .quantity(oi.getQuantity())
                        .build())
                .toList();

        OrderRequest request = OrderRequest.builder()
                .phoneDelivery(originalOrder.getPhoneDelivery())
                .province(originalOrder.getProvince())
                .district(originalOrder.getDistrict())
                .ward(originalOrder.getWard())
                .addressDetail(originalOrder.getAddressDetail())
                .paymentMethodId(originalOrder.getPaymentMethodId())
                .build();

        Order order = createOrder(request, items, userId);
        return OrderResponse.from(order);
    }

    // === Admin methods ===

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderFilterRequest filter, Pageable pageable) {
        return orderRepository.filterOrders(filter, pageable).map(OrderResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdAdmin(Integer orderId) {
        Order order = orderRepository.findByIdWithFullDetails(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        return OrderResponse.from(order);
    }

    @Override
    public OrderResponse updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
        return doUpdateStatus(order, status);
    }

    @Override
    public OrderResponse updateOrderStatus(String orderNumber, String status) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderNumber));
        return doUpdateStatus(order, status);
    }

    private OrderResponse doUpdateStatus(Order order, String status) {
        String oldStatus = order.getStatus().getValue();
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        
        validateStatusTransition(order.getStatus(), newStatus);
        
        // Confirm inventory chỉ khi chuyển từ PENDING → CONFIRMED (COD)
        if (OrderStatus.CONFIRMED.equals(newStatus) && OrderStatus.PENDING.equals(order.getStatus())) {
            List<Map<String, Integer>> confirmItems = order.getOrderItems().stream()
                .map(oi -> Map.of("variantId", oi.getVariantId(), "quantity", oi.getQuantity()))
                .toList();
            try {
                productServiceClient.confirmInventory(confirmItems);
            } catch (Exception e) {
                log.error("Failed to confirm inventory for order {}: {}", order.getId(), e.getMessage());
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        // Publish status updated event
        eventPublisher.publishOrderStatusUpdated(OrderStatusUpdatedEvent.builder()
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .userFullName(order.getUserFullName())
                .oldStatus(oldStatus)
                .newStatus(newStatus.getValue())
                .build());

        return OrderResponse.from(order);
    }

    @Override
    public void bulkUpdateStatus(List<Integer> orderIds, String status) {
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        orderIds.forEach(id -> {
            orderRepository.findById(id).ifPresent(order -> {
                try {
                    validateStatusTransition(order.getStatus(), newStatus);
                    order.setStatus(newStatus);
                } catch (InvalidException e) {
                    log.warn("Skipping order {} - invalid status transition", order.getId());
                }
            });
        });
        orderRepository.saveAll(orderRepository.findAllById(orderIds));
    }

    @Override
    public byte[] exportOrders(OrderFilterRequest filter, String format) {
        // TODO: Implement CSV/Excel export
        return new byte[0];
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse trackOrder(String orderNumber, String email) {
        Order order = orderRepository.findByOrderNumberAndEmail(orderNumber, email)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return OrderResponse.from(order);
    }

    @Override
    public void validateOrder(List<OrderItemRequest> items) {
        List<Integer> variantIds = items.stream().map(OrderItemRequest::getVariantId).toList();
        ApiResponse<List<ProductVariantDTO>> response = productServiceClient.getVariantsByIds(variantIds);
        if (response.getData() == null || response.getData().size() != variantIds.size()) {
            throw new NotFoundException("Some variants not found");
        }
    }

    @Override
    public String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "NEKI-" + timestamp + "-" + random;
    }

    @Override
    public void restoreInventory(Integer variantId, Integer quantity) {
        try {
            productServiceClient.restoreInventory(List.of(Map.of("variantId", variantId, "quantity", quantity)));
        } catch (Exception e) {
            log.error("Failed to restore inventory for variant {}: {}", variantId, e.getMessage());
        }
    }

    private void publishOrderCreatedEvent(Order order) {
        List<OrderCreatedEvent.OrderItemEvent> itemEvents = order.getOrderItems().stream()
                .map(oi -> OrderCreatedEvent.OrderItemEvent.builder()
                        .variantId(oi.getVariantId())
                        .productId(oi.getProductId())
                        .productName(oi.getProductName())
                        .variantInfo(oi.getColorName() + " / " + oi.getSizeName())
                        .quantity(oi.getQuantity())
                        .unitPrice(oi.getUnitPrice())
                        .totalPrice(oi.getTotalPrice())
                        .build())
                .toList();

        String shippingAddress = String.join(", ",
                order.getAddressDetail(), order.getWard(), order.getDistrict(), order.getProvince());

        eventPublisher.publishOrderCreated(OrderCreatedEvent.builder()
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .userFullName(order.getUserFullName())
                .totalAmount(order.getTotalAmount())
                .finalAmount(order.getFinalAmount())
                .shippingAddress(shippingAddress)
                .items(itemEvents)
                .build());
    }

    private void publishOrderCancelledEvent(Order order, String reason) {
        List<OrderCancelledEvent.CancelledItemEvent> cancelledItems = order.getOrderItems().stream()
                .map(oi -> OrderCancelledEvent.CancelledItemEvent.builder()
                        .variantId(oi.getVariantId())
                        .quantity(oi.getQuantity())
                        .build())
                .toList();

        eventPublisher.publishOrderCancelled(OrderCancelledEvent.builder()
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .userFullName(order.getUserFullName())
                .reason(reason)
                .items(cancelledItems)
                .build());
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        Map<OrderStatus, Set<OrderStatus>> allowedTransitions = Map.of(
                OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
                OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
                OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED),
                OrderStatus.DELIVERED, Set.of(),
                OrderStatus.CANCELLED, Set.of());

        if (!allowedTransitions.getOrDefault(current, Set.of()).contains(next)) {
            throw new InvalidException("Invalid status transition");
        }
    }
}
