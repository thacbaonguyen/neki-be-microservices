package com.thacbao.orderservice.service;

import com.thacbao.orderservice.dto.request.DiscountRequest;
import com.thacbao.orderservice.dto.response.DiscountResponse;
import com.thacbao.orderservice.model.Discount;
import com.thacbao.orderservice.model.Order;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountService {
    void create(DiscountRequest request);

    void update(Integer id, DiscountRequest request);

    void delete(Integer id);

    List<DiscountResponse> getAllByType(String discountType);

    Discount validateAndGetDiscount(String code, Integer userId, BigDecimal orderAmount);

    void recordUsage(Discount discount, Integer userId, Order order);
}
