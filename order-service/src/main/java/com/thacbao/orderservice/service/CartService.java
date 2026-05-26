package com.thacbao.orderservice.service;

import com.thacbao.orderservice.dto.request.CartRequest;
import com.thacbao.orderservice.dto.response.CartResponse;

public interface CartService {
    CartResponse addProductToCart(CartRequest request, Integer userId);

    void removeProductFromCart(Integer cartItemId, Integer userId);

    void changeQuantity(Integer cartItemId, Integer quantity, Integer userId);

    void updateProductQuantity(Integer cartItemId, Integer quantity, Integer userId);

    CartResponse getCart(Integer userId);
}
