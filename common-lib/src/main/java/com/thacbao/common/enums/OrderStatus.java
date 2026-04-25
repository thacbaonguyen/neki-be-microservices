package com.thacbao.common.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    SHIPPED("shipped"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }
}
