package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.dto.response.WishListResponse;
import com.thacbao.orderservice.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> addToWishList(
            @PathVariable Integer productId,
            @RequestHeader("X-User-Id") Integer userId) {
        wishListService.addProductToWishList(productId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWishList(
            @PathVariable Integer productId,
            @RequestHeader("X-User-Id") Integer userId) {
        wishListService.removeProductFromWishList(productId, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<WishListResponse>> getWishList(
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(wishListService.getAllWishList(userId)));
    }
}
