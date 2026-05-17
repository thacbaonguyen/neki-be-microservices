package com.thacbao.orderservice.service.impl;

import com.thacbao.common.enums.DiscountType;
import com.thacbao.orderservice.model.Discount;
import com.thacbao.orderservice.service.DiscountCalculationService;
import com.thacbao.orderservice.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiscountCalculationServiceImpl implements DiscountCalculationService {

    private final DiscountService discountService;

    @Override
    public Map<DiscountType, BigDecimal> applyDiscountCode(String discountCode, Integer userId,
            BigDecimal orderAmount, BigDecimal shippingFee) {
        Map<DiscountType, BigDecimal> result = new HashMap<>();

        Discount discount = discountService.validateAndGetDiscount(discountCode, userId, orderAmount);

        if (discount.getDiscountType() == DiscountType.AMOUNT) {
            if (discount.getPercent() != null && discount.getPercent() > 0) {
                // Percentage-based discount
                BigDecimal discountAmount = orderAmount
                        .multiply(BigDecimal.valueOf(discount.getPercent()))
                        .divide(BigDecimal.valueOf(100));
                result.put(DiscountType.AMOUNT, discountAmount);
            } else if (discount.getReduceAmount() != null) {
                // Fixed amount discount
                result.put(DiscountType.AMOUNT, discount.getReduceAmount());
            }
        } else if (discount.getDiscountType() == DiscountType.SHIP) {
            result.put(DiscountType.SHIP, shippingFee);
        }

        return result;
    }
}
