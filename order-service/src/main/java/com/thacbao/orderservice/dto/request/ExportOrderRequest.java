package com.thacbao.orderservice.dto.request;

import lombok.Data;

@Data
public class ExportOrderRequest {
    private OrderFilterRequest orderFilter;
    private String format;
}
