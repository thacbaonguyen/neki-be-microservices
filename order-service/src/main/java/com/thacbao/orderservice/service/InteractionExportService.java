package com.thacbao.orderservice.service;

import com.thacbao.orderservice.dto.response.InteractionBatchDTO;
import com.thacbao.orderservice.dto.response.UserInteractionDTO;

public interface InteractionExportService {
    UserInteractionDTO getUserInteractionProducts(Integer userId);
    InteractionBatchDTO getReviewInteractions(int page, int size);
    InteractionBatchDTO getOrderInteractions(int page, int size);
    InteractionBatchDTO getWishlistInteractions(int page, int size);
}
