package com.thacbao.orderservice.service.impl;

import com.thacbao.common.enums.DiscountType;
import com.thacbao.orderservice.model.Discount;
import com.thacbao.orderservice.service.DiscountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountCalculationServiceImplTest {

    @Mock private DiscountService discountService;
    @InjectMocks private DiscountCalculationServiceImpl service;

    @Test
    void applyDiscountCode_percentage() {
        Discount discount = Discount.builder()
                .discountType(DiscountType.AMOUNT).percent(10).build();
        when(discountService.validateAndGetDiscount("CODE10", 1, BigDecimal.valueOf(200000)))
                .thenReturn(discount);

        Map<DiscountType, BigDecimal> result = service.applyDiscountCode(
                "CODE10", 1, BigDecimal.valueOf(200000), BigDecimal.valueOf(30000));

        assertTrue(result.containsKey(DiscountType.AMOUNT));
        assertEquals(0, BigDecimal.valueOf(20000).compareTo(result.get(DiscountType.AMOUNT)));
    }

    @Test
    void applyDiscountCode_fixedAmount() {
        Discount discount = Discount.builder()
                .discountType(DiscountType.AMOUNT).reduceAmount(BigDecimal.valueOf(50000)).build();
        when(discountService.validateAndGetDiscount("FIXED50", 1, BigDecimal.valueOf(200000)))
                .thenReturn(discount);

        Map<DiscountType, BigDecimal> result = service.applyDiscountCode(
                "FIXED50", 1, BigDecimal.valueOf(200000), BigDecimal.valueOf(30000));

        assertEquals(BigDecimal.valueOf(50000), result.get(DiscountType.AMOUNT));
    }

    @Test
    void applyDiscountCode_freeShipping() {
        Discount discount = Discount.builder()
                .discountType(DiscountType.SHIP).build();
        when(discountService.validateAndGetDiscount("FREESHIP", 1, BigDecimal.valueOf(200000)))
                .thenReturn(discount);

        Map<DiscountType, BigDecimal> result = service.applyDiscountCode(
                "FREESHIP", 1, BigDecimal.valueOf(200000), BigDecimal.valueOf(30000));

        assertEquals(BigDecimal.valueOf(30000), result.get(DiscountType.SHIP));
    }
}
