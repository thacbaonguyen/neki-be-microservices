package com.thacbao.orderservice.service.impl;

import com.thacbao.common.exception.NotFoundException;
import com.thacbao.common.dto.ProductVariantDTO;
import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.client.ProductServiceClient;
import com.thacbao.orderservice.dto.request.CartRequest;
import com.thacbao.orderservice.dto.response.CartResponse;
import com.thacbao.orderservice.model.Cart;
import com.thacbao.orderservice.model.CartItem;
import com.thacbao.orderservice.repository.CartItemRepository;
import com.thacbao.orderservice.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addProductToCart_newCart() {
        CartRequest request = new CartRequest();
        request.setVariantId(1);
        request.setQuantity(2);

        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());
        Cart newCart = Cart.builder().userId(1).cartItems(new HashSet<>()).build();

        newCart.setId(1);
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(cartItemRepository.findByCartIdAndVariantId(1, 1)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));

        CartResponse result = cartService.addProductToCart(request, 1);

        assertNotNull(result);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_existingItem_addsQuantity() {
        CartRequest request = new CartRequest();
        request.setVariantId(1);
        request.setQuantity(3);

        Cart cart = Cart.builder().userId(1).cartItems(new HashSet<>()).build();


        cart.setId(1);
        CartItem existing = CartItem.builder().cart(cart).variantId(1).quantity(2).build();

        existing.setId(1);
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndVariantId(1, 1)).thenReturn(Optional.of(existing));

        cartService.addProductToCart(request, 1);

        assertEquals(5, existing.getQuantity());
        verify(cartItemRepository).save(existing);
    }

    @Test
    void removeProductFromCart_success() {
        CartItem item = CartItem.builder().build();
        when(cartItemRepository.findByIdAndCart_UserId(1, 1)).thenReturn(Optional.of(item));

        cartService.removeProductFromCart(1, 1);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void removeProductFromCart_notFound_throws() {
        when(cartItemRepository.findByIdAndCart_UserId(99, 1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartService.removeProductFromCart(99, 1));
    }

    @Test
    void changeQuantity_increase() {
        CartItem item = CartItem.builder().quantity(3).build();
        item.setId(1);
        when(cartItemRepository.findByIdAndCart_UserId(1, 1)).thenReturn(Optional.of(item));

        cartService.changeQuantity(1, 2, 1);

        assertEquals(5, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    void changeQuantity_decrease_toZero_removes() {
        CartItem item = CartItem.builder().quantity(2).build();

        item.setId(1);
        when(cartItemRepository.findByIdAndCart_UserId(1, 1)).thenReturn(Optional.of(item));

        cartService.changeQuantity(1, -2, 1);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void updateProductQuantity_positive() {
        CartItem item = CartItem.builder().quantity(3).build();

        item.setId(1);
        when(cartItemRepository.findByIdAndCart_UserId(1, 1)).thenReturn(Optional.of(item));

        cartService.updateProductQuantity(1, 5, 1);

        assertEquals(5, item.getQuantity());
        verify(cartItemRepository).save(item);
    }

    @Test
    void updateProductQuantity_zero_removes() {
        CartItem item = CartItem.builder().quantity(3).build();

        item.setId(1);
        when(cartItemRepository.findByIdAndCart_UserId(1, 1)).thenReturn(Optional.of(item));

        cartService.updateProductQuantity(1, 0, 1);

        verify(cartItemRepository).delete(item);
    }

    @Test
    void getCart_enrichesFromProductService() {
        CartItem item = CartItem.builder().variantId(1).quantity(2).build();

        item.setId(1);
        Cart cart = Cart.builder().userId(1).cartItems(new HashSet<>(Set.of(item))).build();

        cart.setId(1);
        item.setCart(cart);
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));

        ProductVariantDTO variant = new ProductVariantDTO();
        variant.setId(1);
        variant.setProductName("Test Product");
        variant.setPrice(BigDecimal.valueOf(50000));
        variant.setColorName("Red");
        variant.setSizeName("M");
        variant.setImageUrl("img.jpg");
        variant.setStockQuantity(10);

        when(productServiceClient.getVariantsByIds(List.of(1))).thenReturn(
                ApiResponse.<List<ProductVariantDTO>>builder().data(List.of(variant)).build());

        CartResponse result = cartService.getCart(1);

        assertNotNull(result);
    }

    @Test
    void getCart_productServiceFails_graceful() {
        CartItem item = CartItem.builder().variantId(1).quantity(2).build();

        item.setId(1);
        Cart cart = Cart.builder().userId(1).cartItems(new HashSet<>(Set.of(item))).build();

        cart.setId(1);
        item.setCart(cart);
        when(cartRepository.findByUserId(1)).thenReturn(Optional.of(cart));
        when(productServiceClient.getVariantsByIds(anyList())).thenThrow(new RuntimeException("Feign error"));

        CartResponse result = cartService.getCart(1);

        assertNotNull(result);
    }

    @Test
    void getCart_createsNewCartIfNotFound() {
        Cart newCart = Cart.builder().userId(1).cartItems(new HashSet<>()).build();

        newCart.setId(1);
        when(cartRepository.findByUserId(1)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        CartResponse result = cartService.getCart(1);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }
}
