package com.thacbao.orderservice.service;

import com.thacbao.common.enums.DiscountType;

import java.math.BigDecimal;
import java.util.Map;

public interface DiscountCalculationService {
    Map<DiscountType, BigDecimal> applyDiscountCode(String discountCode, Integer userId,
            BigDecimal orderAmount, BigDecimal shippingFee);
}
