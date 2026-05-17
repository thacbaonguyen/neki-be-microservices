package com.thacbao.orderservice.service.impl;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.client.ProductServiceClient;
import com.thacbao.orderservice.dto.response.WishListResponse;
import com.thacbao.orderservice.model.Wishlist;
import com.thacbao.orderservice.repository.WishlistRepository;
import com.thacbao.orderservice.service.WishListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WishListServiceImpl implements WishListService {

    private final WishlistRepository wishlistRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    public void addProductToWishList(int productId, Integer userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder().userId(userId).build();
                    return wishlistRepository.save(newWishlist);
                });

        if (!wishlist.getProductIds().contains(productId)) {
            wishlist.getProductIds().add(productId);
            wishlistRepository.save(wishlist);
        }
    }

    @Override
    public void removeProductFromWishList(int productId, Integer userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId).orElse(null);
        if (wishlist != null) {
            wishlist.getProductIds().remove(Integer.valueOf(productId));
            wishlistRepository.save(wishlist);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WishListResponse getAllWishList(Integer userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wishlist newWishlist = Wishlist.builder().userId(userId).build();
                    return wishlistRepository.save(newWishlist);
                });
        WishListResponse response = WishListResponse.from(wishlist);

        // Enrich with product details from product-service
        if (wishlist.getProductIds() != null && !wishlist.getProductIds().isEmpty()) {
            try {
                ApiResponse<List<Map<String, Object>>> productResponse =
                        productServiceClient.getProductsDetailByIds(wishlist.getProductIds());
                if (productResponse != null && productResponse.getData() != null) {
                    response.setProduct(productResponse.getData());
                } else {
                    response.setProduct(Collections.emptyList());
                }
            } catch (Exception e) {
                log.error("Failed to enrich wishlist with product details: {}", e.getMessage());
                response.setProduct(Collections.emptyList());
            }
        } else {
            response.setProduct(Collections.emptyList());
        }

        return response;
    }
}
