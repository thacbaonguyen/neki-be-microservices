package com.thacbao.orderservice.service;

import com.thacbao.orderservice.dto.response.WishListResponse;

public interface WishListService {
    void addProductToWishList(int productId, Integer userId);
    void removeProductFromWishList(int productId, Integer userId);
    WishListResponse getAllWishList(Integer userId);
}
