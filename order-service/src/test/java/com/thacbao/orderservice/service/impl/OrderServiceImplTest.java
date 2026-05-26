package com.thacbao.orderservice.service.impl;

import com.thacbao.common.dto.ProductVariantDTO;
import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.common.enums.OrderStatus;
import com.thacbao.common.exception.InvalidException;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.orderservice.client.ProductServiceClient;
import com.thacbao.orderservice.client.PaymentServiceClient;
import com.thacbao.orderservice.client.UserServiceClient;
import com.thacbao.orderservice.dto.request.CreatePaymentRequest;
import com.thacbao.orderservice.dto.request.OrderFilterRequest;
import com.thacbao.orderservice.dto.request.OrderItemRequest;
import com.thacbao.orderservice.dto.request.OrderRequest;
import com.thacbao.orderservice.dto.response.PaymentMethodResponse;
import com.thacbao.orderservice.dto.response.PaymentResponse;
import com.thacbao.orderservice.dto.response.OrderResponse;
import com.thacbao.orderservice.dto.response.OrderSummaryResponse;
import com.thacbao.orderservice.model.*;
import com.thacbao.orderservice.repository.CartRepository;
import com.thacbao.orderservice.repository.OrderRepository;
import com.thacbao.orderservice.service.DiscountCalculationService;
import com.thacbao.orderservice.service.DiscountService;
import com.thacbao.orderservice.service.ShippingService;
import com.thacbao.common.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CartRepository cartRepository;
    @Mock private ProductServiceClient productServiceClient;
    @Mock private PaymentServiceClient paymentServiceClient;
    @Mock private UserServiceClient userServiceClient;
    @Mock private DiscountService discountService;
    @Mock private DiscountCalculationService discountCalculationService;
    @Mock private ShippingService shippingService;
    @Mock private OrderEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest orderRequest;
    private ProductVariantDTO variant;

    @BeforeEach
    void setUp() {
        orderRequest = OrderRequest.builder()
                .province("HCM").district("Q1").ward("P.BenNghe")
                .addressDetail("123 Main St").phoneDelivery("0123456789")
                .paymentMethodId(1).build();

        variant = new ProductVariantDTO();
        variant.setId(1);
        variant.setProductId(10);
        variant.setProductName("Test Product");
        variant.setColorName("Red");
        variant.setSizeName("M");
        variant.setPrice(BigDecimal.valueOf(100000));
        variant.setSalePrice(BigDecimal.valueOf(80000));
        variant.setImageUrl("http://img.com/1.jpg");
    }

    private void setupFeignMocks() {
        UserDTO userDTO = UserDTO.builder().id(1).email("test@test.com").fullName("Test User").build();
        when(userServiceClient.getUserById(1)).thenReturn(
                ApiResponse.<UserDTO>builder().data(userDTO).build());
        when(productServiceClient.getVariantsByIds(anyList())).thenReturn(
                ApiResponse.<List<ProductVariantDTO>>builder().data(List.of(variant)).build());
        doNothing().when(productServiceClient).reserveInventory(anyList());
        when(shippingService.calculateShippingFee(anyString(), anyString(), any())).thenReturn(BigDecimal.valueOf(30000));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            if (o.getId() == null) o.setId(1);
            return o;
        });
        when(paymentServiceClient.getPaymentMethods()).thenReturn(
                ApiResponse.<List<PaymentMethodResponse>>builder()
                        .data(List.of(PaymentMethodResponse.builder()
                                .id(1)
                                .name("Cash")
                                .isActive(true)
                                .build()))
                        .build());
        when(paymentServiceClient.createPayment(any(CreatePaymentRequest.class))).thenReturn(
                ApiResponse.<PaymentResponse>builder()
                        .data(PaymentResponse.builder().id(1).orderNumber("NEKI-001").build())
                        .build());
    }

    @Test
    void createOrderFromCart_success() {
        Cart cart = Cart.builder().userId(1).build();

        cart.setId(1);
        CartItem ci = CartItem.builder().variantId(1).quantity(2).cart(cart).build();
        cart.setCartItems(new HashSet<>(Set.of(ci)));

        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        setupFeignMocks();

        OrderResponse result = orderService.createOrderFromCart(orderRequest, 1);

        assertNotNull(result);
        verify(cartRepository).save(cart);
        assertTrue(cart.getCartItems().isEmpty());
    }

    @Test
    void createOrderFromCart_cartNotFound_throws() {
        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.createOrderFromCart(orderRequest, 1));
    }

    @Test
    void createOrderFromCart_emptyCart_throws() {
        Cart cart = Cart.builder().userId(1).cartItems(new HashSet<>()).build();

        cart.setId(1);
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        assertThrows(IllegalStateException.class, () -> orderService.createOrderFromCart(orderRequest, 1));
    }

    @Test
    void createOrderFromSelectedItems_success() {
        List<OrderItemRequest> items = List.of(OrderItemRequest.builder().variantId(1).quantity(1).build());
        setupFeignMocks();

        OrderResponse result = orderService.createOrderFromSelectedItems(orderRequest, items, 1);

        assertNotNull(result);
    }

    @Test
    void buyNow_success() {
        OrderItemRequest item = OrderItemRequest.builder().variantId(1).quantity(1).build();
        setupFeignMocks();

        OrderResponse result = orderService.buyNow(orderRequest, item, 1);

        assertNotNull(result);
    }

    @Test
    void createOrder_variantsNotFound_throws() {
        UserDTO userDTO = UserDTO.builder().id(1).email("test@test.com").fullName("Test").build();
        when(userServiceClient.getUserById(1)).thenReturn(
                ApiResponse.<UserDTO>builder().data(userDTO).build());
        when(productServiceClient.getVariantsByIds(anyList())).thenReturn(
                ApiResponse.<List<ProductVariantDTO>>builder().data(null).build());

        List<OrderItemRequest> items = List.of(OrderItemRequest.builder().variantId(1).quantity(1).build());

        assertThrows(NotFoundException.class, () -> orderService.createOrderFromSelectedItems(orderRequest, items, 1));
    }

    @Test
    void createOrder_reserveInventoryFails_compensates() {
        UserDTO userDTO = UserDTO.builder().id(1).email("test@test.com").fullName("Test").build();
        when(userServiceClient.getUserById(1)).thenReturn(
                ApiResponse.<UserDTO>builder().data(userDTO).build());
        when(productServiceClient.getVariantsByIds(anyList())).thenReturn(
                ApiResponse.<List<ProductVariantDTO>>builder().data(List.of(variant)).build());
        doNothing().when(productServiceClient).reserveInventory(anyList());
        when(shippingService.calculateShippingFee(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Shipping error"));

        List<OrderItemRequest> items = List.of(OrderItemRequest.builder().variantId(1).quantity(1).build());

        assertThrows(RuntimeException.class, () -> orderService.createOrderFromSelectedItems(orderRequest, items, 1));
        verify(productServiceClient).restoreInventory(anyList());
    }

    @Test
    void getOrderById_success() {
        Order order = Order.builder().userId(1).status(OrderStatus.PENDING)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findByIdWithFullDetails(1)).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderById(1, 1);

        assertNotNull(result);
    }

    @Test
    void getOrderById_accessDenied_throws() {
        Order order = Order.builder().userId(2).status(OrderStatus.PENDING)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findByIdWithFullDetails(1)).thenReturn(Optional.of(order));

        assertThrows(SecurityException.class, () -> orderService.getOrderById(1, 1));
    }

    @Test
    void getOrderById_notFound_throws() {
        when(orderRepository.findByIdWithFullDetails(999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById(999, 1));
    }

    @Test
    void getOrderByOrderNumber_success() {
        Order order = Order.builder().userId(1).orderNumber("NEKI-001")
                .status(OrderStatus.PENDING).orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findByOrderNumber("NEKI-001")).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderByOrderNumber("NEKI-001", 1);

        assertNotNull(result);
    }

    @Test
    void getMyOrders_returnsPage() {
        Order order = Order.builder().userId(1).status(OrderStatus.PENDING)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findByUserId(eq(1), any(Pageable.class))).thenReturn(page);

        Page<OrderSummaryResponse> result = orderService.getMyOrders(1, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getMyOrdersByStatus_returnsPage() {
        Order order = Order.builder().userId(1).status(OrderStatus.PENDING)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.findByUserIdAndStatus(eq(1), eq(OrderStatus.PENDING), any())).thenReturn(page);

        Page<OrderSummaryResponse> result = orderService.getMyOrdersByStatus(1, "PENDING", Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void cancelOrder_success() {
        Order order = Order.builder().userId(1).status(OrderStatus.PENDING)
                .orderItems(Set.of(OrderItem.builder().variantId(1).quantity(2).build())).build();

        order.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1, "Changed mind", 1);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(productServiceClient).restoreInventory(anyList());
        verify(eventPublisher).publishOrderCancelled(any());
    }

    @Test
    void cancelOrder_notPending_throws() {
        Order order = Order.builder().userId(1).status(OrderStatus.CONFIRMED)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> orderService.cancelOrder(1, "reason", 1));
    }

    @Test
    void cancelOrder_accessDenied_throws() {
        Order order = Order.builder().userId(2).status(OrderStatus.PENDING)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(SecurityException.class, () -> orderService.cancelOrder(1, "reason", 1));
    }

    @Test
    void updateOrderStatus_byId_success() {
        Order order = Order.builder().userId(1).status(OrderStatus.PENDING)
                .orderNumber("NEKI-001").userEmail("test@test.com").userFullName("Test")
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OrderResponse result = orderService.updateOrderStatus(1, "CONFIRMED");

        assertNotNull(result);
        verify(eventPublisher).publishOrderStatusUpdated(any());
    }

    @Test
    void updateOrderStatus_invalidTransition_throws() {
        Order order = Order.builder().userId(1).status(OrderStatus.DELIVERED)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));

        assertThrows(InvalidException.class, () -> orderService.updateOrderStatus(1, "PENDING"));
    }

    @Test
    void updateOrderStatus_byOrderNumber() {
        Order order = Order.builder().userId(1).status(OrderStatus.PENDING)
                .orderNumber("NEKI-001").userEmail("test@test.com").userFullName("Test")
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findByOrderNumber("NEKI-001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        OrderResponse result = orderService.updateOrderStatus("NEKI-001", "CONFIRMED");

        assertNotNull(result);
    }

    @Test
    void updateOrderStatus_pendingToConfirmed_confirmsInventory() {
        Order order = Order.builder().userId(1).status(OrderStatus.PENDING)
                .orderNumber("NEKI-001").userEmail("test@test.com").userFullName("Test")
                .orderItems(Set.of(OrderItem.builder().variantId(1).quantity(2).build())).build();

        order.setId(1);
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        orderService.updateOrderStatus(1, "CONFIRMED");

        verify(productServiceClient).confirmInventory(anyList());
    }

    @Test
    void bulkUpdateStatus() {
        Order o1 = Order.builder().status(OrderStatus.PENDING).build();

        o1.setId(1);
        Order o2 = Order.builder().status(OrderStatus.DELIVERED).build();

        o2.setId(2);
        when(orderRepository.findById(1)).thenReturn(Optional.of(o1));
        when(orderRepository.findById(2)).thenReturn(Optional.of(o2));
        when(orderRepository.findAllById(anyList())).thenReturn(List.of(o1, o2));

        orderService.bulkUpdateStatus(List.of(1, 2), "CONFIRMED");

        assertEquals(OrderStatus.CONFIRMED, o1.getStatus());
        assertEquals(OrderStatus.DELIVERED, o2.getStatus());
    }

    @Test
    void trackOrder_success() {
        Order order = Order.builder().userId(1).status(OrderStatus.SHIPPED)
                .orderNumber("NEKI-001").orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findByOrderNumberAndEmail("NEKI-001", "test@test.com"))
                .thenReturn(Optional.of(order));

        OrderResponse result = orderService.trackOrder("NEKI-001", "test@test.com");

        assertNotNull(result);
    }

    @Test
    void trackOrder_notFound_throws() {
        when(orderRepository.findByOrderNumberAndEmail("INVALID", "test@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.trackOrder("INVALID", "test@test.com"));
    }

    @Test
    void validateOrder_success() {
        when(productServiceClient.getVariantsByIds(List.of(1, 2))).thenReturn(
                ApiResponse.<List<ProductVariantDTO>>builder()
                        .data(List.of(variant, variant)).build());

        assertDoesNotThrow(() -> orderService.validateOrder(List.of(
                OrderItemRequest.builder().variantId(1).build(),
                OrderItemRequest.builder().variantId(2).build())));
    }

    @Test
    void validateOrder_someNotFound_throws() {
        when(productServiceClient.getVariantsByIds(List.of(1, 2))).thenReturn(
                ApiResponse.<List<ProductVariantDTO>>builder()
                        .data(List.of(variant)).build());

        assertThrows(NotFoundException.class, () -> orderService.validateOrder(List.of(
                OrderItemRequest.builder().variantId(1).build(),
                OrderItemRequest.builder().variantId(2).build())));
    }

    @Test
    void generateOrderNumber_hasCorrectFormat() {
        String number = orderService.generateOrderNumber();

        assertTrue(number.startsWith("NEKI-"));
        assertTrue(number.length() > 10);
    }

    @Test
    void restoreInventory_callsClient() {
        orderService.restoreInventory(1, 5);

        verify(productServiceClient).restoreInventory(anyList());
    }

    @Test
    void restoreInventory_clientFails_logsError() {
        doThrow(new RuntimeException("Feign error")).when(productServiceClient).restoreInventory(anyList());

        assertDoesNotThrow(() -> orderService.restoreInventory(1, 5));
    }

    @Test
    void exportOrders_returnsEmptyBytes() {
        byte[] result = orderService.exportOrders(new OrderFilterRequest(), "csv");

        assertEquals(0, result.length);
    }

    @Test
    void getAllOrders_admin() {
        Order order = Order.builder().status(OrderStatus.PENDING)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        Page<Order> page = new PageImpl<>(List.of(order));
        when(orderRepository.filterOrders(any(), any())).thenReturn(page);

        Page<OrderResponse> result = orderService.getAllOrders(new OrderFilterRequest(), Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getOrderByIdAdmin_success() {
        Order order = Order.builder().status(OrderStatus.PENDING)
                .orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findByIdWithFullDetails(1)).thenReturn(Optional.of(order));

        OrderResponse result = orderService.getOrderByIdAdmin(1);

        assertNotNull(result);
    }

    @Test
    void reOrder_success() {
        Order originalOrder = Order.builder().userId(1).status(OrderStatus.DELIVERED)
                .phoneDelivery("0123456789").province("HCM").district("Q1").ward("P1")
                .addressDetail("123").paymentMethodId(1)
                .orderItems(Set.of(OrderItem.builder().variantId(1).quantity(1).build())).build();

        originalOrder.setId(1);
        when(orderRepository.findByIdWithFullDetails(1)).thenReturn(Optional.of(originalOrder));
        setupFeignMocks();

        OrderResponse result = orderService.reOrder(1, 1);

        assertNotNull(result);
    }

    @Test
    void reOrder_accessDenied_throws() {
        Order order = Order.builder().userId(2).orderItems(new HashSet<>()).build();

        order.setId(1);
        when(orderRepository.findByIdWithFullDetails(1)).thenReturn(Optional.of(order));

        assertThrows(SecurityException.class, () -> orderService.reOrder(1, 1));
    }
}
