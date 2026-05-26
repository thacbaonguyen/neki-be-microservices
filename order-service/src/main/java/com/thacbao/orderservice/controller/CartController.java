package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.dto.request.CartRequest;
import com.thacbao.orderservice.dto.response.CartResponse;
import com.thacbao.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody CartRequest request,
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.addProductToCart(request, userId)));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @PathVariable Integer cartItemId,
            @RequestHeader("X-User-Id") Integer userId) {
        cartService.removeProductFromCart(cartItemId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/items/{cartItemId}/quantity")
    public ResponseEntity<ApiResponse<Void>> changeQuantity(
            @PathVariable Integer cartItemId,
            @RequestParam Integer quantity,
            @RequestHeader("X-User-Id") Integer userId) {
        cartService.changeQuantity(cartItemId, quantity, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/items/{cartItemId}/quantity")
    public ResponseEntity<ApiResponse<Void>> updateQuantity(
            @PathVariable Integer cartItemId,
            @RequestParam Integer quantity,
            @RequestHeader("X-User-Id") Integer userId) {
        cartService.updateProductQuantity(cartItemId, quantity, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCart(userId)));
    }
}
