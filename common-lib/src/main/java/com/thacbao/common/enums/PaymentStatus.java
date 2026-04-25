package com.thacbao.common.enums;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    PENDING("PENDING"),
    CANCELLED("CANCELLED"),
    UNDERPAID("UNDERPAID"),
    PAID("PAID"),
    EXPIRED("EXPIRED"),
    PROCESSING("PROCESSING"),
    FAILED("FAILED");

    private final String value;

    public static PaymentStatus fromValue(String value) {
        for (PaymentStatus item : values()) {
            if (item.value.equals(value.toUpperCase())) {
                return item;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

    PaymentStatus(final String value) {
        this.value = value;
    }
}
