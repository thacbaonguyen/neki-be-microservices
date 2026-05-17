package com.thacbao.orderservice.service.impl;

import com.thacbao.common.exception.NotFoundException;
import com.thacbao.orderservice.dto.request.CartRequest;
import com.thacbao.orderservice.dto.response.CartItemResponse;
import com.thacbao.orderservice.dto.response.CartResponse;
import com.thacbao.orderservice.model.Cart;
import com.thacbao.orderservice.model.CartItem;
import com.thacbao.orderservice.repository.CartItemRepository;
import com.thacbao.orderservice.repository.CartRepository;
import com.thacbao.orderservice.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final com.thacbao.orderservice.client.ProductServiceClient productServiceClient;

    @Override
    public CartResponse addProductToCart(CartRequest request, Integer userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndVariantId(cart.getId(), request.getVariantId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variantId(request.getVariantId())
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(newItem);
        }

        return getCart(userId);
    }

    @Override
    public void removeProductFromCart(Integer cartItemId, Integer userId) {
        CartItem cartItem = cartItemRepository.findByIdAndCart_UserId(cartItemId, userId)
                .orElseThrow(() -> new NotFoundException("Cart item not found: " + cartItemId));
        cartItemRepository.delete(cartItem);
    }

    @Override
    public void changeQuantity(Integer cartItemId, Integer quantity, Integer userId) {
        CartItem cartItem = cartItemRepository.findByIdAndCart_UserId(cartItemId, userId)
                .orElseThrow(() -> new NotFoundException("Cart item not found: " + cartItemId));
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        if (cartItem.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItemRepository.save(cartItem);
        }
    }

    @Override
    public void updateProductQuantity(Integer cartItemId, Integer quantity, Integer userId) {
        CartItem cartItem = cartItemRepository.findByIdAndCart_UserId(cartItemId, userId)
                .orElseThrow(() -> new NotFoundException("Cart item not found: " + cartItemId));
        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    @Override
    @Transactional
    public CartResponse getCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().userId(userId).build();
                    return cartRepository.save(newCart);
                });
                
        CartResponse response = CartResponse.from(cart);
        
        // Enrich via Product Service if there are items
        if (response.getCartItems() != null && !response.getCartItems().isEmpty()) {
            java.util.List<Integer> variantIds = response.getCartItems().stream()
                    .map(com.thacbao.orderservice.dto.response.CartItemResponse::getVariantId)
                    .collect(java.util.stream.Collectors.toList());
                    
            try {
                com.thacbao.common.dto.response.ApiResponse<java.util.List<com.thacbao.common.dto.ProductVariantDTO>> apiRes = 
                        productServiceClient.getVariantsByIds(variantIds);
                        
                if (apiRes != null && apiRes.getData() != null) {
                    java.util.Map<Integer, com.thacbao.common.dto.ProductVariantDTO> variantMap = 
                            apiRes.getData().stream()
                                  .collect(java.util.stream.Collectors.toMap(com.thacbao.common.dto.ProductVariantDTO::getId, v -> v));
                                  
                    response.getCartItems().forEach(item -> {
                        com.thacbao.common.dto.ProductVariantDTO variant = variantMap.get(item.getVariantId());
                        if (variant != null) {
                            item.setProductName(variant.getProductName());
                            // item.setProductSlug(variant.getSlug()); // ProductVariantDTO lacks slug, handled on FE or DB directly
                            item.setProductImage(variant.getImageUrl());
                            item.setColorName(variant.getColorName());
                            item.setSizeName(variant.getSizeName());
                            
                            // Note: We're mapping the variant's sale price or base product price as needed.
                            if (variant.getSalePrice() != null) {
                                item.setPrice(variant.getSalePrice());
                            } else if (variant.getPrice() != null) {
                                item.setPrice(variant.getPrice());
                            } else {
                                item.setPrice(java.math.BigDecimal.ZERO);
                            }
                            item.setStockQuantity(variant.getStockQuantity());
                        }
                    });
                }
            } catch (Exception e) {
                log.error("Failed to enrich cart items from product-service: {}", e.getMessage());
            }
        }
        
        return response;
    }
}
