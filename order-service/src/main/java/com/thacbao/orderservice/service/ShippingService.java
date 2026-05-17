package com.thacbao.orderservice.service;

import java.math.BigDecimal;

public interface ShippingService {
    BigDecimal calculateShippingFee(String district, String ward, BigDecimal orderAmount);
}
