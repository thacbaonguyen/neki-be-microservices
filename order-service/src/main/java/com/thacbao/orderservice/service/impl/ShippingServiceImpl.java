package com.thacbao.orderservice.service.impl;

import com.thacbao.orderservice.service.ShippingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ShippingServiceImpl implements ShippingService {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(500000);
    private static final BigDecimal DEFAULT_SHIPPING_FEE = BigDecimal.valueOf(30000);
    private static final BigDecimal INNER_CITY_FEE = BigDecimal.valueOf(15000);

    @Override
    public BigDecimal calculateShippingFee(String district, String ward, BigDecimal orderAmount) {
        if (orderAmount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }

        // Simple district-based calculation
        if (district != null && (district.contains("Quận") || district.contains("quận"))) {
            return INNER_CITY_FEE;
        }

        return DEFAULT_SHIPPING_FEE;
    }
}
