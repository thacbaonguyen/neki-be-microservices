package com.thacbao.orderservice.dto.request;

import lombok.Data;

@Data
public class UpdateOrderStatusRequest {
    private String status;
}
