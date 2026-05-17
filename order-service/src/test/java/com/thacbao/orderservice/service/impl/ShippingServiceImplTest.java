package com.thacbao.orderservice.service.impl;

import com.thacbao.orderservice.service.ShippingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceImplTest {

    @InjectMocks
    private ShippingServiceImpl shippingService;

    @Test
    void calculateShippingFee_returnsPositiveValue() {
        BigDecimal fee = shippingService.calculateShippingFee("Q1", "P.BenNghe", BigDecimal.valueOf(100000));

        assertNotNull(fee);
        assertTrue(fee.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void calculateShippingFee_largeOrder() {
        BigDecimal fee = shippingService.calculateShippingFee("Q1", "P.BenNghe", BigDecimal.valueOf(10000000));

        assertNotNull(fee);
    }
}
